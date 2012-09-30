/**
 * Created on 2007-5-20
 * @author pizza(pizzamx@gmail.com)
 */
package subtitle.action;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import subtitle.model.Moment;
import subtitle.model.SubtitleUnit;
import subtitle.utils.TimelineHelper;

public class AppendLineAction extends AbstractSubtitleAction {
//    public AppendLineAction() {
//        System.out.println("Instanitiating AppendLineAction");
//        setId( "subtitle.edit.AppendLine" );
//        setActionDefinitionId( "subtitle.edit.AppendLine" );
//    }
    public void run(IAction action) {
        ISelection selection = tableViewer.getSelection();
        SubtitleUnit line = (SubtitleUnit)((IStructuredSelection)selection).getFirstElement();
        final int index = items.indexOf( line );
        final SubtitleUnit newLine = new SubtitleUnit();
        newLine.setOriginal( "" );
        int from, to;

        abstract class appendOperation extends AbstractOperation {
            public appendOperation(String label) {
                super( label );
            }
            public appendOperation() {
                this( "向后插入" );
            }
            protected abstract void doAppend();
            public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
                doAppend();
                tableViewer.setInput( items );
                return Status.OK_STATUS;
            }

            public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
                return execute( monitor, info );
            }

            public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
                items.remove( newLine );
                tableViewer.setInput( items );
                return Status.OK_STATUS;
            }

        }
        // 如果是最后一行
        if (index == items.size() - 1) {
            from = line.getEnd().toInt() + 100;
            to = from + 1000;
            newLine.setTimeline( TimelineHelper.generateTimeline( new Moment( from ), new Moment( to ) ) );
            editor.setDirty( true, new appendOperation() {
                protected void doAppend() {
                    items.add( newLine );
                }
            } );
        } else {
            SubtitleUnit nextLine = (SubtitleUnit)items.get( index + 1 );
            int distance = TimelineHelper.getDistance( nextLine.getStart(), line.getEnd() );
            if (distance <= 100) {
                MessageDialog.openInformation( editor.getSite().getShell(), "无法插入", "前后两行时间间距太小，只有" + ((double)distance / 100) + "秒\n这种情况下调整时间轴也许会更好" );
                return;
            } else if (distance < 1200) {
                from = line.getEnd().toInt() + 100;
                to = nextLine.getStart().toInt() - 100;
            } else {
                from = line.getEnd().toInt() + 100;
                to = from + 1000;
            }
            newLine.setTimeline( TimelineHelper.generateTimeline( new Moment( from ), new Moment( to ) ) );
            editor.setDirty( true, new appendOperation() {
                protected void doAppend() {
                    items.add( index + 1, newLine );
                }
            } );
        }
    }
    public void selectionChanged(IAction action, ISelection selection) {
        // SubtitleUnit line =
        // (SubtitleUnit)((IStructuredSelection)selection).getFirstElement();
        // if (line != null) {
        // int index = items.indexOf( line );
        // action.setEnabled( index != items.size() - 1 );
        // }
    }

}
