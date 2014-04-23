package org.dcm4chee.wizard.util;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.validation.IValidator;
import org.dcm4chee.wizard.model.GenericConfigNodeModel;

public class FormUtils {

    // ugly helper
    @SuppressWarnings("unchecked")
    public static void addGenericField(Form form, String fieldName, GenericConfigNodeModel model, boolean textArea, boolean required) {
        
        form.add(new Label(fieldName+".label", new ResourceModel(
                "dicom.edit.xds."+fieldName+".label")));
        
        FormComponent<String> formField;
        
        if (textArea)
            formField = new TextArea<String>(fieldName, model); else
            formField = new TextField<String>(fieldName, model);
            
        formField.setType(String.class);
        formField.setRequired(required);
        formField.add((IValidator<String>) model);
        form.add(formField);
    }

}
