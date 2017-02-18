package lemming.ui.panel;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.protocol.http.servlet.MultipartServletWebRequest;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.template.PackageTextTemplate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A panel providing drag and drop file uploads.
 */
public class DropzonePanel extends Panel {
    /**
     * Interface to implement a submit listener.
     */
    public interface SubmitListener {
        /**
         * Called on submit of the dropzone panel.
         *
         * @param target target that produces an Ajax response
         * @param fileItem object representing a file for a form item
         */
        void onSubmit(AjaxRequestTarget target, FileItem fileItem);
    }

    private FileItem fileItem;

    /**
     * Creates a dropzone panel.
     *
     * @param id ID of the panel
     */
    public DropzonePanel(String id) {
        super(id);
        setMarkupId(id);
        FileUploadField fileUploadField = new FileUploadField("file");
        MarkupContainer messageContainer = new WebMarkupContainer("message");
        MarkupContainer errorMessageContainer = new WebMarkupContainer("errorMessage");

        add(fileUploadField);
        add(messageContainer);
        add(errorMessageContainer);
        add(new FileDropBehavior(fileUploadField.getMarkupId()));
    }

    /**
     * Registers a component as submit listener.
     *
     * @param listener a component implementing interface SubmitListener
     * @throws IllegalArgumentException
     */
    public void registerSubmitListener(Component listener) throws IllegalArgumentException {
        if (listener instanceof SubmitListener) {
            listener.add(new FileSubmitBehavior(getMarkupId()));
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Shows an info message.
     *
     * @param target target that produces an Ajax response
     */
    public void showMessage(AjaxRequestTarget target) {
        String javaScript = "jQuery('#" + getMarkupId() + " .filename').hide().text(''); " +
                "jQuery('#" + getMarkupId() + " .message').fadeIn();";
        target.appendJavaScript(javaScript);
    }

    /**
     * Sets an error message if an exception occurred.
     *
     * @param target target that produces an Ajax response
     * @param message error message
     */
    public void setErrorMessage(AjaxRequestTarget target, String message) {
        String javaScript = "jQuery('#" + getMarkupId() + " .filename').hide().text(''); " +
                "jQuery('#" + getMarkupId() + " .error-message').text('" + message + "').fadeIn();";
        target.appendJavaScript(javaScript);
    }

    /**
     * Implementation of a file drop behavior.
     */
    private class FileDropBehavior extends Behavior {
        /**
         * ID of a file upload field.
         */
        private String fileUploadFieldMarkupId;

        /**
         * Creates a file drop behavior.
         *
         * @param fileUploadFieldMarkupId ID of a file upload field
         */
        public FileDropBehavior(String fileUploadFieldMarkupId) {
            super();
            this.fileUploadFieldMarkupId = fileUploadFieldMarkupId;
        }

        /**
         * Renders to the web response what the component wants to contribute.
         *
         * @param component component object
         * @param response response object
         */
        @Override
        public void renderHead(Component component, IHeaderResponse response) {
            PackageResourceReference cssReference = new CssResourceReference(DropzonePanel.class,
                    "styles/dropzone.css");
            PackageTextTemplate javascriptTemplate = new PackageTextTemplate(DropzonePanel.class,
                    "scripts/dropzone-formdata.js");
            Map<String, Object> map = new HashMap<>();

            map.put("dropzoneId", component.getMarkupId());
            map.put("fileUploadId", fileUploadFieldMarkupId);
            response.render(CssReferenceHeaderItem.forReference(cssReference));
            response.render(OnDomReadyHeaderItem.forScript(javascriptTemplate.asString(map)));
        }
    }

    /**
     * Implementation of a file submit behavior.
     */
    private class FileSubmitBehavior extends AbstractDefaultAjaxBehavior {
        /**
         * ID of a dropzone.
         */
        private String dropzoneId;

        /**
         * Creates a file submit behavior.
         *
         * @param dropzoneId ID of a dropzone
         */
        public FileSubmitBehavior(String dropzoneId) {
            this.dropzoneId = dropzoneId;
        }

        /**
         * Renders to the web response what the component wants to contribute.
         *
         * @param component component object
         * @param response response object
         */
        @Override
        public void renderHead(Component component, IHeaderResponse response) {
            PackageTextTemplate javascriptTemplate = new PackageTextTemplate(DropzonePanel.class, "scripts" +
                    "/dropzone-submit.js");
            Map<String, Object> map = new HashMap<>();

            map.put("dropzoneId", dropzoneId);
            map.put("submitButtonId", component.getMarkupId());
            map.put("callbackUrl", getCallbackUrl());
            response.render(JavaScriptHeaderItem.forScript(javascriptTemplate.asString(map), "dropzone-submit"));
        }

        /**
         * Generates a response.
         *
         * @param target target that produces an Ajax response
         */
        @Override
        protected void respond(AjaxRequestTarget target) {
            ServletWebRequest request = (ServletWebRequest) RequestCycle.get().getRequest();

            try {
                Iterator<String> parameterIterator = request.getRequestParameters().getParameterNames().iterator();

                if (parameterIterator.hasNext()) {
                    MultipartServletWebRequest multipartRequest = request
                            .newMultipartWebRequest(Bytes.megabytes(10), "ignored");
                    multipartRequest.parseFileParts();
                    List<FileItem> fileItems = multipartRequest.getFiles().get("file");

                    if (fileItems != null) {
                        if (fileItems.size() > 0) {
                            fileItem = fileItems.get(0);
                        }
                    }
                }
            } catch (FileUploadException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Implementation of a file submit behavior.
     */
    private class FileProcessBehavior extends AbstractDefaultAjaxBehavior {
        /**
         * ID of a dropzone.
         */
        private String dropzoneId;

        /**
         * Creates a file process behavior.
         *
         * @param dropzoneId ID of a dropzone
         */
        public FileProcessBehavior(String dropzoneId) {
            this.dropzoneId = dropzoneId;
        }

        /**
         * Generates a response.
         *
         * @param target target that produces an Ajax response
         */
        @Override
        protected void respond(AjaxRequestTarget target) {
            ServletWebRequest request = (ServletWebRequest) RequestCycle.get().getRequest();

            try {
                Iterator<String> parameterIterator = request.getRequestParameters().getParameterNames().iterator();

                if (parameterIterator.hasNext()) {
                    MultipartServletWebRequest multipartRequest = request
                            .newMultipartWebRequest(Bytes.megabytes(10), "ignored");
                    multipartRequest.parseFileParts();
                    List<FileItem> fileItems = multipartRequest.getFiles().get("file");
                    SubmitListener listener = (SubmitListener) getComponent();

                    if (fileItems != null) {
                        if (fileItems.size() > 0) {
                            listener.onSubmit(target, fileItems.get(0));
                        }
                    }
                }
            } catch (FileUploadException e) {
                e.printStackTrace();
            }
        }
    }
}
