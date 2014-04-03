package au.com.vaadinutils.jasper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import javax.activation.DataSource;
import javax.persistence.EntityManager;

import net.sf.jasperreports.engine.JRAbstractExporter;
import net.sf.jasperreports.engine.JRBand;
import net.sf.jasperreports.engine.JRElement;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRStaticText;
import net.sf.jasperreports.engine.JRTextField;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignElement;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRExportProgressMonitor;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.fill.AsynchronousFillHandle;
import net.sf.jasperreports.engine.fill.FillListener;
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRSwapFile;
import net.sf.jasperreports.web.servlets.AsyncJasperPrintAccessor;
import net.sf.jasperreports.web.servlets.ReportExecutionStatus;

import org.apache.commons.mail.ByteArrayDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.vaadinutils.dao.Transaction;
import au.com.vaadinutils.jasper.parameter.ReportChooser;
import au.com.vaadinutils.jasper.parameter.ReportParameter;
import au.com.vaadinutils.jasper.servlet.VaadinJasperPrintServlet;
import au.com.vaadinutils.jasper.ui.JasperReportDataProvider;
import au.com.vaadinutils.listener.CompleteListener;

import com.google.gwt.thirdparty.guava.common.base.Preconditions;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.WrappedSession;
import com.vaadin.ui.UI;

public class JasperManager implements Runnable
{
	private static transient Logger logger = LogManager.getLogger(JasperManager.class);

	private final JasperReport jasperReport;
	private final Map<String, Object> boundParams = new HashMap<String, Object>();
	private final JasperSettings settings;
	private final EntityManager em;
	private final String reportFilename;

	private AsynchronousFillHandle fillHandle;

	volatile private boolean stop;

	private String reportTitle;

	private final static Semaphore concurrentLimit = new Semaphore(Math.max(
			Runtime.getRuntime().availableProcessors() / 2, 1));

	public enum Disposition
	{
		inline, mixed

	}

	public enum OutputFormat
	{
		PDF
		{
			@Override
			public String getMimeType()
			{
				return "application/pdf";
			}
		},
		HTML
		{
			@Override
			public String getMimeType()
			{
				return "text/html";
			}
		},
		CSV
		{
			@Override
			public String getMimeType()
			{
				return "text/csv";
			}
		};

		abstract public String getMimeType();
	}

	/**
	 * 
	 * @param jasperReport
	 *            path to jasper report.
	 * @throws JRException
	 */
	public JasperManager(EntityManager em, String reportFileName, String reportTitle, JasperSettings settings)
	{
		this.reportTitle = reportTitle;
		String reportDesignName = reportFileName.substring(0, reportFileName.indexOf("."));

		Preconditions.checkArgument(settings.getReportFile(reportDesignName + ".jrxml").exists(),
				"The passed Jasper Report File doesn't exist: "
						+ settings.getReportFile(reportDesignName).getAbsolutePath());

		this.em = em;
		this.settings = settings;
		this.reportFilename = reportFileName;

		try
		{
			// compileReport(getDesignFile(sourcePath, reportDesignName),
			// sourcePath, sourcePath, reportDesignName);

			File sourcePath = settings.getReportFile(reportFileName).getParentFile();
			JasperReportCompiler jasperReportCompiler = new JasperReportCompiler();
			JasperDesign designFile = jasperReportCompiler.getDesignFile(sourcePath, reportDesignName);

			String templateName = settings.getHeaderFooterTemplateName();
			if (templateName != null)
			{
				JasperDesign headerTemplate = jasperReportCompiler.getDesignFile(sourcePath, templateName);

				replaceHeader(designFile, headerTemplate);
			}
			setCSVOptions(designFile);

			jasperReportCompiler.compileReport(designFile, sourcePath, sourcePath, reportDesignName);

			this.jasperReport = (JasperReport) JRLoader.loadObject(settings.getReportFile(reportFileName));

		}
		catch (Throwable e)
		{
			logger.error(e, e);
			throw new RuntimeException("Bad report compilation");
		}

	}

	private void replaceHeader(JasperDesign designFile, JasperDesign template)
	{
		JRBand title = template.getTitle();
		JRDesignBand newTitle = new JRDesignBand();
		
		int margin = 50;
		designFile.setLeftMargin(margin);
		designFile.setRightMargin(margin);
		designFile.setTopMargin(margin);
		designFile.setBottomMargin(margin);
		
		
		designFile.setPageWidth(designFile.getPageWidth()+(margin*2));


		
		int maxY = determineSizeOfTitleTemplateAndReplaceTitlePlaceHolder(designFile, title, newTitle, margin);

		maxY += 2;

		mergeExistingTitleWithTemplateTitle(designFile, newTitle, maxY);

		JRBand footer = replaceFooterWithTemplateFooter(designFile, template, margin);

		designFile.setPageFooter(footer);

	}

	private JRBand replaceFooterWithTemplateFooter(JasperDesign designFile, JasperDesign template, int margin)
	{
		JRBand footer = template.getPageFooter();
		for (JRElement element : footer.getElements())
		{
			if (element instanceof JRDesignElement)
			{
				JRDesignElement de = (JRDesignElement) element;
				if (element instanceof JRTextField)
				{
					JRTextField st = (JRTextField) element;

					JRDesignExpression expr = (JRDesignExpression) st.getExpression();
					expr.setText("\"" + reportTitle + "\"+" + expr.getText());
					st.setWidth((designFile.getPageWidth() - st.getX())-(margin*2) );

				}

			}

		}
		return footer;
	}

	private void mergeExistingTitleWithTemplateTitle(JasperDesign designFile, JRDesignBand newTitle, int yoffset)
	{
		int maxY = 0;
		for (JRElement element : designFile.getTitle().getElements())
		{

			JRDesignElement de = (JRDesignElement) element;
			de.setY(de.getY() + yoffset);
			maxY = Math.max(maxY, de.getY() + element.getHeight());
			newTitle.addElement(element);
		}
		newTitle.setHeight(Math.max(maxY + 2,yoffset+2));
		designFile.setTitle(newTitle);

	}

	private int determineSizeOfTitleTemplateAndReplaceTitlePlaceHolder(JasperDesign designFile, JRBand header, JRDesignBand newTitle,
			int margin)
	{
		int maxY = 0;
		for (JRElement element : header.getElements())
		{
			if (element instanceof JRDesignElement)
			{
				JRDesignElement de = (JRDesignElement) element;
				if (element instanceof JRStaticText)
				{
					JRStaticText st = (JRStaticText) element;
					if (st.getText().equalsIgnoreCase("report name place holder"))
					{
						st.setText(reportTitle);
						st.setWidth((designFile.getPageWidth() - st.getX())-(margin*2) );
					}
				}

				maxY = Math.max(maxY, de.getY() + de.getHeight());

				newTitle.addElement(de);
			}

		}
		return maxY;
	}

	private void setCSVOptions(JasperDesign designFile)
	{
		designFile.setProperty("net.sf.jasperreports.export.csv.exclude.origin.keep.first.band.columnHeader",
				"columnHeader");
		designFile.setProperty("net.sf.jasperreports.export.csv.exclude.origin.band.title", "title");
		designFile.setProperty("net.sf.jasperreports.export.csv.exclude.origin.band.pageHeader", "pageHeader");
		designFile.setProperty("net.sf.jasperreports.export.csv.exclude.origin.band.pageFooter", "pageFooter");
		designFile.setProperty("net.sf.jasperreports.export.csv.exclude.origin.band.footer", "footer");
	}

	public JRParameter[] getParameters()
	{
		return jasperReport.getParameters();
	}

	public boolean paramExists(String parameterName)
	{
		return getParameter(parameterName) != null;
	}

	public JRParameter getParameter(String parameterName)
	{
		JRParameter result = null;

		for (JRParameter param : jasperReport.getParameters())
		{
			if (param.getName().equals(parameterName))
			{
				result = param;
				break;
			}
		}
		return result;

	}

	/**
	 * Binds a value to a report parameter.
	 * 
	 * Essentially a report can have a no. of named parameters which are used to
	 * filter the report or display on the report. This method allows you to set
	 * the parameters value at runtime.
	 * 
	 * @param parameterName
	 * @param parameterValue
	 */
	public void bindParameter(ReportParameter<?> param)
	{
		// ReportChooser is not actually a report parameter
		if (!(param instanceof ReportChooser))
		{
			String tmpParam = param.getParameterName();
			// specific work around for prefixed report parameters
			if (tmpParam.startsWith("ReportParameter"))
			{
				tmpParam = tmpParam.substring("ReportParameter".length(), tmpParam.length());
			}

			if (!paramExists(tmpParam))
			{
				logger.warn("The passed Jasper Report parameter: " + param.getParameterName()
						+ " does not existing on the Report");
			}

			boundParams.put(tmpParam, param.getValue());
		}
	}

	// /**
	// * Binds a value to a report parameter.
	// *
	// * Essentially a report can have a no. of named parameters which are used
	// to
	// * filter the report or display on the report. This method allows you to
	// * pass in a map (name, value) of parameter value at runtime.
	// *
	// * @param parameters
	// * a map of name/value pairs to bind to report parameters of the
	// * given names.
	// */
	//
	// private void bindParameters(Map<String, Object> parameters)
	// {
	// for (String parameterName : parameters.keySet())
	// {
	// Preconditions.checkArgument(paramExists(parameterName),
	// "The passed Jasper Report parameter: "
	// + parameterName + " does not existing on the Report");
	//
	// boundParams.put(parameterName, parameters.get(parameterName));
	// }
	// }

	private JasperPrint fillReport() throws JRException
	{
		JasperPrint jasper_print;

		Transaction t = new Transaction(em);
		try
		{

			java.sql.Connection connection = em.unwrap(java.sql.Connection.class);

			fillHandle = AsynchronousFillHandle.createHandle(jasperReport, boundParams, connection);
			fillHandle.addFillListener(new FillListener()
			{

				@Override
				public void pageUpdated(JasperPrint jasperPrint, int pageIndex)
				{
					status = "Filling page " + pageIndex;
				}

				@Override
				public void pageGenerated(JasperPrint jasperPrint, int pageIndex)
				{
					status = "Generating page " + pageIndex;
				}
			});

			AsyncJasperPrintAccessor asyncAccessor = new AsyncJasperPrintAccessor(fillHandle);

			if (!stop)
			{
				fillHandle.startFill();

			}

			// jasper_print = JasperFillManager.fillReport(jasperReport,
			// boundParams, connection);
			while (asyncAccessor.getReportStatus().getStatus() == ReportExecutionStatus.Status.RUNNING)
			{
				try
				{
					Thread.sleep(100);
				}
				catch (InterruptedException e)
				{
					logger.error(e, e);
				}
			}

			t.commit();
			return asyncAccessor.getFinalJasperPrint();
		}
		finally
		{

			t.close();
		}

	}

	public void cancelPrint()
	{
		stop = true;

	}

	public JasperSettings getSettings()
	{
		return this.settings;
	}

	private PipedInputStream inputStream;

	private PipedOutputStream outputStream;

	private OutputFormat exportMethod;

	CountDownLatch writerStartedBarrier;
	CountDownLatch completeBarrier;
	Map<String, byte[]> images;

	private JasperReportDataProvider dataProvider;

	private Collection<ReportParameter<?>> params;

	private DataSource[] imagesrcs;

	private CompleteListener completeListener;

	volatile private String status;

	public RenderedReport export(JasperReportDataProvider dataProvider, OutputFormat exportMethod,
			Collection<ReportParameter<?>> params) throws InterruptedException
	{

		exportAsync(dataProvider, exportMethod, params, null);
		completeBarrier.await();
		return new RenderedReport(inputStream, imagesrcs, exportMethod);

	}

	public PipedInputStream exportAsync(JasperReportDataProvider dataProvider, OutputFormat exportMethod,
			Collection<ReportParameter<?>> params, CompleteListener completeListener) throws InterruptedException
	{

		WrappedSession session = UI.getCurrent().getSession().getSession();
		images = new ConcurrentHashMap<String, byte[]>();
		session.setAttribute(VaadinJasperPrintServlet.IMAGES_MAP, images);

		stop = false;
		completeBarrier = new CountDownLatch(1);
		writerStartedBarrier = new CountDownLatch(1);
		this.dataProvider = dataProvider;
		this.completeListener = completeListener;

		if (params == null)
		{
			params = new LinkedList<ReportParameter<?>>();
		}
		this.params = params;

		if (concurrentLimit.tryAcquire())
		{
			inputStream = new PipedInputStream();
			this.exportMethod = exportMethod;
			new Thread(this).start();
			writerStartedBarrier.await();
		}
		else
		{
			completeListener.complete();
			throw new RuntimeException("Too busy now, please try to run this report again later");
		}
		return inputStream;
	}

	@Override
	public void run()
	{

		JRSwapFileVirtualizer fileVirtualizer = null;
		try
		{
			outputStream = new PipedOutputStream(inputStream);
			writerStartedBarrier.countDown();

			status = "Gathering report data phase 1, please be patient";

			dataProvider.initDBConnection();
			params.addAll(dataProvider.prepareData(params, getReportFilename()));

			logger.warn("Running report " + getReportFilename());
			for (ReportParameter<?> param : params)
			{
				bindParameter(param);
				logger.warn(param.getParameterName() + " " + param.getValue());
			}

			dataProvider.prepareForOutputFormat(exportMethod);
			CustomJRHyperlinkProducerFactory.setUseCustomHyperLinks(true);

			JRAbstractExporter exporter = null;

			status = "Gathering report data phase 2, please be patient";

			// use file virtualizer to prevent out of heap
			String fileName = "/tmp";
			JRSwapFile file = new JRSwapFile(fileName, 100, 10);
			fileVirtualizer = new JRSwapFileVirtualizer(500, file);
			boundParams.put(JRParameter.REPORT_VIRTUALIZER, fileVirtualizer);

			if (stop)
			{
				return;
			}

			JasperPrint jasper_print = fillReport();

			String renderedName = this.jasperReport.getName();

			switch (exportMethod)
			{
			case HTML:
			{
				exporter = new JRHtmlExporter();

				exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasper_print);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, outputStream);
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_MAP, images);

				String context = VaadinServlet.getCurrent().getServletContext().getContextPath();
				int contextIndex = Page.getCurrent().getLocation().toString().lastIndexOf(context);
				String baseurl = Page.getCurrent().getLocation().toString()
						.substring(0, contextIndex + context.length() + 1);

				String imageUrl = baseurl + "VaadinJasperPrintServlet?image=";

				exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, imageUrl);

				renderedName += ".htm";
				break;
			}
			case PDF:
			{
				exporter = new JRPdfExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasper_print);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, outputStream);
				renderedName += ".pdf";
				break;
			}
			case CSV:
			{
				exporter = new JRCsvExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasper_print);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, outputStream);
				renderedName += ".csv";
				break;
			}
			default:
			{
				throw new RuntimeException("Unsupported export option " + exportMethod);
			}

			}

			if (logger.isDebugEnabled())
				for (Entry<JRExporterParameter, Object> param : exporter.getParameters().entrySet())
				{
					logger.debug("{} : {}", param.getKey(), param.getValue());

				}

			imagesrcs = (images.size() <= 0) ? null : new DataSource[images.size()];
			if (imagesrcs != null)
			{
				int xi = 0;
				for (Map.Entry<String, byte[]> entry : images.entrySet())
				{
					ByteArrayDataSource image = new ByteArrayDataSource(entry.getValue(), "image/gif");
					image.setName(entry.getKey());
					imagesrcs[xi++] = image;
				}
			}

			if (stop)
			{
				return;
			}
			createPageProgressMonitor(exporter);

			exporter.exportReport();

			Thread.sleep(750);
			status = "Cleaning up";

		}
		catch (Exception e)
		{
			logger.error(e, e);
		}
		finally
		{
			try
			{
				outputStream.close();
			}
			catch (IOException e)
			{
				logger.error(e, e);
			}

			concurrentLimit.release();
			dataProvider.cleanup();
			if (fileVirtualizer != null)
			{
				fileVirtualizer.cleanup();
			}
			CustomJRHyperlinkProducerFactory.setUseCustomHyperLinks(false);
			dataProvider.closeDBConnection();
			completeBarrier.countDown();
			if (completeListener != null)
			{
				completeListener.complete();
			}
		}

	}

	private void createPageProgressMonitor(JRAbstractExporter exporter)
	{
		exporter.setParameter(JRExporterParameter.PROGRESS_MONITOR, new JRExportProgressMonitor()
		{
			int pageCount = 0;

			@Override
			public void afterPageExport()
			{
				pageCount++;
				status = "Rendering page " + pageCount;

			}
		});
	}

	protected static String getJasperFile(final String dir, final String name) throws IOException
	{
		final File file = new File(dir, name);
		if (!file.exists())
			throw new FileNotFoundException(file.toString());
		return file.getCanonicalPath();
	}

	public String getReportFilename()
	{
		return reportFilename;
	}

	public String getReportTitle()
	{
		return jasperReport.getName();
	}

	public String getStatus()
	{
		return status;
	}

}
