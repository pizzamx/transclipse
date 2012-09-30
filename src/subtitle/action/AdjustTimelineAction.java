/**
 * Created on 2007-5-22
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
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import subtitle.model.Moment;
import subtitle.model.SubtitleUnit;
import subtitle.ui.AdjustTimelineDialog;

public class AdjustTimelineAction extends AbstractSubtitleAction {
    abstract class AdjustOperation extends AbstractOperation {
        public AdjustOperation() {
            super( "调整时间轴" );

        }
        public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
            return execute( monitor, info );
        }
    }

    AdjustTimelineDialog dialog;

    public void run(IAction action) {
        ISelection selection = tableViewer.getSelection();
        final SubtitleUnit unit = (SubtitleUnit)((IStructuredSelection)selection).getFirstElement();
        final int index = items.indexOf( unit );
        dialog = new AdjustTimelineDialog( editor.getSite(), unit, editor.getItems() );
        if (dialog.open() == InputDialog.OK) {
            int tabIndex = dialog.index;
            switch (tabIndex) {
            case 0:// 当前
                final Moment newStart = dialog.getMoment( 0 ),
                newEnd = dialog.getMoment( 1 );
                if (newStart != null || newEnd != null) {
                    final Moment oldStart = unit.getStart(), oldEnd = unit.getEnd();
                    editor.setDirty( true, new AdjustOperation() {
                        public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
                            if (newStart != null) {
                                unit.setStart( newStart );
                            }
                            if (newEnd != null) {
                                unit.setEnd( newEnd );
                            }
                            unit.refresh();
                            tableViewer.update( unit, null );
                            return Status.OK_STATUS;
                        }
                        public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
                            unit.setStart( oldStart );
                            unit.setEnd( oldEnd );
                            unit.refresh();
                            tableViewer.update( unit, null );
                            return Status.OK_STATUS;
                        }
                    } );
                    editor.lineFocused( index );
                }
                break;
            case 1:// 往后
                final Moment newHead = dialog.getMoment( 2 );
                if (newHead != null) {
                    final int size = items.size() - items.indexOf( unit ), difference = newHead.toInt() - unit.getStart().toInt();
                    // todo:如果往前调整要注意不要重叠
                    // todo:存放int会不会占用内存更小？囧rz
                    final Moment[] oldMoments = new Moment[size * 2];// 存放所有后续句子的开始结束时间
                    editor.setDirty( true, new AdjustOperation() {
                        public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
                            SubtitleUnit tempUnit;
                            Moment oldStart, oldEnd;
                            for (int i = 0; i < size; i++) {
                                tempUnit = (SubtitleUnit)items.get( i + index );
                                oldStart = tempUnit.getStart();
                                oldEnd = tempUnit.getEnd();
                                oldMoments[i * 2] = oldStart;
                                oldMoments[i * 2 + 1] = oldEnd;
                                tempUnit.setStart( new Moment( oldStart, difference ) );
                                tempUnit.setEnd( new Moment( oldEnd, difference ) );
                                tempUnit.refresh();
                                tableViewer.update( tempUnit, null );
                            }
                            return Status.OK_STATUS;
                        }
                        public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
                            SubtitleUnit tempUnit;
                            Moment oldStart, oldEnd;
                            for (int i = 0; i < size; i++) {
                                tempUnit = (SubtitleUnit)items.get( i + index );
                                oldStart = oldMoments[i * 2];
                                oldEnd = oldMoments[i * 2 + 1];
                                tempUnit.setStart( oldStart );
                                tempUnit.setEnd( oldEnd );
                                tempUnit.refresh();
                                tableViewer.update( tempUnit, null );
                            }
                            return Status.OK_STATUS;
                        }
                    } );
                    editor.lineFocused( items.indexOf( unit ) );
                }
                break;
            case 2:// 首末
                break;
            }
        }
        // dialog.close();
    }
    public void selectionChanged(IAction action, ISelection selection) {
    }

}
