/**
 * Created on 2007-5-12
 * @author pizza(pizzamx@gmail.com)
 */
package subtitle.action;

import java.util.Arrays;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import subtitle.model.SubtitleUnit;
import subtitle.utils.TimelineHelper;

public class MergeAction extends AbstractSubtitleAction {

    public void run(IAction action) {
        ISelection selection = tableViewer.getSelection();
        final Object[] objects = ((IStructuredSelection)selection).toArray();
        final int firstLine = items.indexOf( objects[0] );
        final int size = items.indexOf( objects[objects.length - 1] ) - firstLine + 1;
        editor.setDirty( true, new AbstractOperation( "�ϲ�" ) {
            public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
                StringBuffer orgi = new StringBuffer(), translated = new StringBuffer();
                // ��һ��������ѡ������Ҫ�Ѳ���������Ҳ������ȥ
                SubtitleUnit unit;
                int j = firstLine;
                // merge
                for (int i = 0; i < size; i++) {
                    unit = (SubtitleUnit)items.get( j + i );
                    orgi.append( unit.getOriginal() ).append( ' ' );// Ӣ�ļӸ��ո�
                    translated.append( unit.getTranslation() );
                }
                // keep this one
                unit = new SubtitleUnit();
                unit.setTimeline( TimelineHelper.merge( TimelineHelper.split( ((SubtitleUnit)objects[0]).getTimeline() )[0], TimelineHelper
                        .split( ((SubtitleUnit)objects[objects.length - 1]).getTimeline() )[1] ) );
                unit.setOriginal( orgi.toString() );
                unit.setTranslation( translated.toString() );
                // remove
                items.add( j, unit );
                items.subList( j + 1, j + size + 1 ).clear();
                // ��tableViewer.refresh()�Ļ���Ų���ˢ��
                tableViewer.setInput( items );
                return Status.OK_STATUS;
            }

            public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
                return execute( monitor, info );
            }

            public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
                items.addAll( firstLine, Arrays.asList( objects ) );
                items.remove( firstLine + size );
                tableViewer.setInput( items );
                return Status.OK_STATUS;
            }
        } );
    }

    public void selectionChanged(IAction action, ISelection selection) {
    }

}
