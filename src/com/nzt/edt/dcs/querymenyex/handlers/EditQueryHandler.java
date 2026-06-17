package com.nzt.edt.dcs.querymenyex.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.xtext.ui.editor.embedded.EmbeddedEditor;
import org.eclipse.xtext.ui.editor.model.XtextDocument;

import com._1c.g5.v8.bm.integration.IBmEditingContext;
import com._1c.g5.v8.dt.core.V8Commands;
import com._1c.g5.v8.dt.core.platform.IV8Project;
import com._1c.g5.v8.dt.dcs.model.schema.DataCompositionSchemaDataSetQuery;
import com._1c.g5.v8.dt.dcs.model.schema.DataSet;
import com._1c.g5.v8.dt.dcs.ui.DataCompositionSchemaEditor;
import com._1c.g5.v8.dt.dcs.ui.DataCompositionSchemaControlContext;
import com._1c.g5.v8.dt.dcs.ui.DcsEvent;
import com._1c.g5.v8.dt.dcs.ui.DcsEvent.DcsEventType;
import com._1c.g5.v8.dt.dcs.ui.EditorPage;
import com._1c.g5.v8.dt.dcs.ui.datasets.DataSets;
import com._1c.g5.v8.dt.qw.ui.dialogs.QueryTextEditDialog;
import com._1c.g5.v8.dt.ui.util.DtHandlerUtil;

public class EditQueryHandler extends AbstractHandler {

    private static void debugInfo(String msg) {
        if (BuildConfig.DEBUG) Platform.getLog(EditQueryHandler.class).info(msg);
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        var log = Platform.getLog(getClass());

        DataCompositionSchemaEditor dcsEditor = DtHandlerUtil.getActiveEditor(
            event, DataCompositionSchemaEditor.class);
        if (dcsEditor == null) {
            log.warn("DCS editor not found");
            return null;
        }
        debugInfo("DCS editor — OK");

        DataCompositionSchemaControlContext context = dcsEditor.getControlContext();

        DataSets dataSets = null;
        for (EditorPage page : dcsEditor.getPages()) {
            if (page instanceof DataSets ds) { dataSets = ds; break; }
        }
        if (dataSets == null) {
            log.warn("DataSets page not found");
            return null;
        }

        EmbeddedEditor embeddedEditor = dataSets.getQueryEditor();
        if (embeddedEditor == null) {
            log.warn("Query editor not found");
            return null;
        }

        IV8Project v8Project = dcsEditor.getV8project();
        if (v8Project == null) {
            log.warn("V8Project not found");
            return null;
        }
        IProject project = v8Project.getProject();

        Shell shell = HandlerUtil.getActiveShell(event);
        if (shell == null || shell.isDisposed()) {
            log.warn("Active shell not found");
            return null;
        }

        XtextDocument document = (XtextDocument) embeddedEditor.getDocument();
        String initialText = document.get();
        debugInfo("query length=" + initialText.length());

        QueryTextEditDialog dialog = new QueryTextEditDialog(project, shell, initialText, true, 0, true);
        int dialogResult = dialog.open();

        if (dialogResult == Dialog.OK) {
            String newText = dialog.getQueryText();
            if (!newText.equals(initialText)) {
                document.set(newText);
                debugInfo("document.set() done");

                if (context != null) {
                    DataSet currentDataSet = dataSets.getCurrentDataSet();
                    if (currentDataSet instanceof DataCompositionSchemaDataSetQuery queryDataSet) {
                        IBmEditingContext editingContext = context.getEditingContext();
                        EStructuralFeature queryFeature = queryDataSet.eClass().getEStructuralFeature("query");
                        V8Commands.executeSet(editingContext, queryDataSet, queryFeature, newText);

                        context.notify(new DcsEvent(DcsEventType.DATASETS_QUERY_CHANGED, queryDataSet));
                        debugInfo("query updated via V8Commands + notify");
                    } else {
                        log.warn("currentDataSet is not DataCompositionSchemaDataSetQuery");
                    }
                }
            }
        }

        return null;
    }
}
