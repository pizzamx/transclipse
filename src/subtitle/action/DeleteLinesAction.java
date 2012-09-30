/**
 * Created on 2007-5-15
 * @author pizza(pizzamx@gmail.com)
 */
package subtitle.action;

import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

public class DeleteLinesAction extends AbstractSubtitleAction {

    public void run(IAction action) {
        ISelection selection = tableViewer.getSelection();
        final List selectedLines = ((IStructuredSelection)selection).toList();
        final int[] indices = new int[selectedLines.size()];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = items.indexOf( selectedLines.get( i ) );
        }
        editor.setDirty( true, new AbstractOperation( "del" ) {
            public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
                items.removeAll( selectedLines );
                tableViewer.setInput( items );
                table.setSelection( indices[0] );
                editor.lineFocused( indices[0] );
                return Status.OK_STATUS;
            }

            public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
                return execute( monitor, info );
            }

            public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
                for (int i = 0; i < indices.length; i++) {
                    items.add( indices[i], selectedLines.get( i ) );
                }
                tableViewer.setInput( items );
                return Status.OK_STATUS;
            }
        } );
    }

    public void selectionChanged(IAction action, ISelection selection) {
    }

}
