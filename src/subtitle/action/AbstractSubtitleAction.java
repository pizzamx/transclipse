/**
 * Created on 2007-5-17
 * @author pizza(pizzamx@gmail.com)
 */
package subtitle.action;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import subtitle.ui.SubtitleEditor;

public abstract class AbstractSubtitleAction extends Action implements IEditorActionDelegate {

    protected SubtitleEditor editor;

    protected List items;

    protected TableViewer tableViewer;

    protected Table table;
    
    public AbstractSubtitleAction() {
        super();
    }

    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        this.editor = (SubtitleEditor)targetEditor;
        // ÎªÉ¶»áÊÇnullÄØ£¿
        if (editor != null) {
            items = editor.getItems();
            tableViewer = editor.getTableViewer();
            table = editor.getTable();
        }
    }

}