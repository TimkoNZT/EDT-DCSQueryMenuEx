package com.nzt.edt.dcs.querybutton.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.xtext.ui.editor.embedded.EmbeddedEditor;
import org.eclipse.xtext.ui.editor.model.XtextDocument;

import com._1c.g5.v8.dt.qw.ui.dialogs.QueryTextEditDialog;

public class EditQueryHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        EmbeddedEditor embeddedEditor = findEmbeddedEditor(event);
        if (embeddedEditor == null)
            return null;

        Shell shell = HandlerUtil.getActiveShell(event);
        if (shell == null || shell.isDisposed())
            return null;

        XtextDocument document = (XtextDocument) embeddedEditor.getDocument();
        String initialText = document.get();

        IProject project = findProject(event);

        QueryTextEditDialog dialog = new QueryTextEditDialog(project, shell, initialText, true, 0, false);
        if (dialog.open() == Dialog.OK) {
            document.set(dialog.getQueryText());
        }

        return null;
    }

    private static EmbeddedEditor findEmbeddedEditor(ExecutionEvent event) {
        Object variable = HandlerUtil.getVariable(event, "activeEmbeddedEditor");
        if (variable instanceof EmbeddedEditor editor)
            return editor;
        return null;
    }

    private static IProject findProject(ExecutionEvent event) {
        IEditorInput input = HandlerUtil.getActiveEditorInput(event);
        if (input instanceof IFileEditorInput fileInput) {
            IFile file = fileInput.getFile();
            if (file != null)
                return file.getProject();
        }
        return null;
    }
}
