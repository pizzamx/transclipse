package subtitle.action;

import java.io.File;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;

import subtitle.DiskFileInputEditor;
import subtitle.SubtitleConstants;
import subtitle.ui.SubtitleEditor;

public class OpenTranslatedSubtitleAction extends Action {

    private final IWorkbenchWindow window;

    public OpenTranslatedSubtitleAction(String text, IWorkbenchWindow window) {
        super( text );
        this.window = window;
        // // The id is used to refer to the action in a menu or toolbar
        setId( SubtitleConstants.CMD_OPEN_ORIGINAL_SUBTITLE );
        // // Associate the action with a pre-defined command, to allow key
        // bindings.
        setActionDefinitionId( SubtitleConstants.CMD_OPEN_ORIGINAL_SUBTITLE );
        // setImageDescriptor( Activator.getImageDescriptor(
        // "/icons/sample2.gif" ) );
    }

    public void run() {
//        IEditorPart editorPart = window.getActivePage().getActiveEditor();
//        if (editorPart != null) {
//            FileDialog fd = new FileDialog( window.getShell(), SWT.OPEN );
//            fd.setText( "´ò¿ªÒëÎÄ" );
//            fd.setFilterExtensions( new String[] { "*.srt" } );
//            String selected = fd.open();
//            if (selected != null) {
//                ((SubtitleEditor)editorPart).openTranslation( new DiskFileInputEditor( new File( selected ) ) );
//            }
//        }
    }
}