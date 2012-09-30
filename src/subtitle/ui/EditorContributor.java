/**
 * Created on 2007-4-29
 * @author pizza(pizzamx@gmail.com)
 */
package subtitle.ui;

import java.util.Map;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.RetargetAction;
import org.eclipse.ui.part.EditorActionBarContributor;

public class EditorContributor extends EditorActionBarContributor {
    SubtitleEditor editor;

    MenuManager menuMgr;

    RetargetAction undoAction, redoAction, deleteAction;

    RetargetAction undoHandler, redoHandler;

    public void dispose() {
        getPage().removePartListener( undoAction );
        getPage().removePartListener( redoAction );
    }
    public void init(IActionBars bars, IWorkbenchPage page) {
        super.init( bars, page );
    }
    public void setActiveEditor(IEditorPart targetEditor) {
        Map registry = ((SubtitleEditor)targetEditor).getRegistry();
        IActionBars bars = getActionBars();
        bars.setGlobalActionHandler( undoAction.getId(), (IAction)registry.get( undoAction.getId() ) );
        bars.setGlobalActionHandler( redoAction.getId(), (IAction)registry.get( redoAction.getId() ) );
        // bars.setGlobalActionHandler( ActionFactory.DELETE.getId(),
        // deleteAction );

    }
    public void contributeToCoolBar(ICoolBarManager coolBarManager) {
        redoAction = (RetargetAction)ActionFactory.REDO.create( getPage().getWorkbenchWindow() );
        undoAction = (RetargetAction)ActionFactory.UNDO.create( getPage().getWorkbenchWindow() );
        IToolBarManager toolBarManager = getActionBars().getToolBarManager();
        toolBarManager.add( undoAction );
        toolBarManager.add( redoAction );
        //
        // ToolBarContributionItem item =
        // (ToolBarContributionItem)coolBarManager.find( "subtitle.test" );
        // IToolBarManager toolBarManager = item.getToolBarManager();
        // toolBarManager.prependToGroup( "additions", redoAction );
        // toolBarManager.prependToGroup( "additions", undoAction );
    }
}
