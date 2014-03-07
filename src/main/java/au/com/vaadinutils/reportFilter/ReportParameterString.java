package au.com.vaadinutils.reportFilter;

import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;

public class ReportParameterString extends ReportParameter
{

	protected TextField field;

	public ReportParameterString(String caption, String parameterName)
	{
		super( parameterName);
		field = new TextField();
		field.setCaption(caption);
	}

	@Override
	protected String getValue()
	{
		return field.getValue();
	}

	@Override
	public Component getComponent()
	{
		return field;
	}

	@Override
	public boolean shouldExpand()
	{
		return false;
	}

}
