package au.com.vaadinutils.crud.splitFields;

import com.vaadin.ui.Label;

public class SplitColorPicker extends ColorPickerField implements SplitField
{
	private static final long serialVersionUID = -1573292123807845727L;
	private Label label;

	public SplitColorPicker(String label)
	{
		this.label = new Label(label);
		setCaption(label);
	}

	@Override
	public void setVisible(boolean visible)
	{
		label.setVisible(visible);
		super.setVisible(visible);
	}

	@Override
	public Label getLabel()
	{
		return label;
	}

	@Override
	public String getCaption()
	{
		return label.getValue();
	}
	
	@Override
	public void hideLabel()
	{
		setCaption(null);
		
	}
}
