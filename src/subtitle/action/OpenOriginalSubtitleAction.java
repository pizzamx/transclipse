package subtitle.action;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import subtitle.Activator;
import subtitle.DiskFileInputEditor;
import subtitle.SubtitleConstants;
import subtitle.ui.SubtitleEditor;

public class OpenOriginalSubtitleAction extends Action {

    private final IWorkbenchWindow window;

    public OpenOriginalSubtitleAction(String text, IWorkbenchWindow window) {
        super( text );
        this.window = window;
        // // The id is used to refer to the action in a menu or toolbar
        setId( SubtitleConstants.CMD_OPEN_ORIGINAL_SUBTITLE );
        // // Associate the action with a pre-defined command, to allow key
        // bindings.
        setActionDefinitionId( SubtitleConstants.CMD_OPEN_ORIGINAL_SUBTITLE );
        setImageDescriptor( Activator.getImageDescriptor( "/icons/open.gif" ) );
    }

    public void run() {
        FileDialog fd = new FileDialog( window.getShell(), SWT.OPEN );
        fd.setText( "打开原文" );
        fd.setFilterExtensions( new String[] { "*.srt" } );
        String selected = fd.open();
        if (selected != null) {
            File file = new File( selected );
            String abs = file.getParent() + System.getProperty( "file.separator" );
            String fileName = file.getName(), baseName;
            // 不管打开哪一个效果都是一样D
            if (fileName.endsWith( ".ref.srt" ) || fileName.endsWith( ".chs.srt" )) {
                baseName = fileName.substring( 0, fileName.length() - 8 );
            } else {
                baseName = fileName.substring( 0, fileName.length() - (fileName.endsWith( ".eng.srt" ) ? 8 : 4) );
            }
            // 拷贝一份到.ref.srt文件，之后于翻译字幕一同修改，原文字幕不动
            String refName = baseName + ".ref.srt";
            File reference = new File( abs + refName );
            if (!reference.exists()) {
                InputStream in = null;
                OutputStream out = null;
                try {
                    reference.createNewFile();
                    in = new BufferedInputStream( new FileInputStream( file ) );
                    out = new BufferedOutputStream( new FileOutputStream( reference ) );
                    for (int c = in.read(); c != -1; c = in.read()) {
                        out.write( c );
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    MessageDialog.openError( window.getShell(), "打开字幕", "无法创建新文件\n" + reference.getName() );
                    return;
                } finally {
                    try {
                        if (in != null) {
                            in.close();
                        }
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            // 创建一个译文文件
            String chsName = baseName + ".chs.srt";
            File translation = new File( abs + chsName );
            if (!translation.exists()) {
                try {
                    translation.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    MessageDialog.openError( window.getShell(), "打开字幕", "无法创建新文件\n" + translation.getName() );
                    return;
                }
            }
            try {
                window.getActivePage().openEditor( new DiskFileInputEditor( reference, translation ), SubtitleEditor.ID );
            } catch (PartInitException e) {
                e.printStackTrace();
            }
        }
    }
}