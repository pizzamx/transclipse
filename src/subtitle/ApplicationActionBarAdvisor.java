package subtitle;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.internal.provisional.action.ToolBarContributionItem2;
import org.eclipse.jface.internal.provisional.action.ToolBarManager2;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

import subtitle.action.AppendLineAction;
import subtitle.action.OpenOriginalSubtitleAction;

/**
 * An action bar advisor is responsible for creating, adding, and disposing of
 * the actions added to a workbench window. Each window will be populated with
 * new actions.
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

    // Actions - important to allocate these only in makeActions, and then use
    // them
    // in the fill methods. This ensures that the actions aren't recreated
    // when fillActionBars is called with FILL_PROXY.
    private IWorkbenchAction exitAction, aboutAction, saveAction, prefAction, redoAction, undoAction;

    private Action openOriginalAction, openTranslatedAction;

    public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
        super( configurer );
    }

    protected void makeActions(final IWorkbenchWindow window) {
        openOriginalAction = new OpenOriginalSubtitleAction( "打开原文", window );
        register( openOriginalAction );

        undoAction = ActionFactory.UNDO.create( window );
        register( undoAction );
        redoAction = ActionFactory.REDO.create( window );
        register( redoAction );

        saveAction = ActionFactory.SAVE.create( window );
        register( saveAction );

        // prefAction = ActionFactory.PREFERENCES.create( window );
        // prefAction.setText( "设置" );
        // register( prefAction );

        aboutAction = ActionFactory.ABOUT.create( window );
        aboutAction.setText( "关于" );
        aboutAction.setImageDescriptor( Activator.getImageDescriptor( "/icons/info.gif" ) );
        register( aboutAction );

    }

    protected void fillMenuBar(IMenuManager menuBar) {
    }

    protected void fillCoolBar(ICoolBarManager coolBar) {
        // IActionBarConfigurer2 actionBarConfigurer =
        // (IActionBarConfigurer2)getActionBarConfigurer();
        // IToolBarManager toolbar = actionBarConfigurer.createToolBarManager();
        IToolBarManager toolbar = new ToolBarManager2( SWT.FLAT | SWT.RIGHT );
        toolbar.add( openOriginalAction );
        toolbar.add( new GroupMarker( IWorkbenchActionConstants.MB_ADDITIONS ) );
        // toolbar.add( prefAction );
        toolbar.add( aboutAction );
        coolBar.add( new ToolBarContributionItem2( toolbar, "subtitle.test" ) );
    }
}
