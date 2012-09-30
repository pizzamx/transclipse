/**
 * Created on 2007-5-20
 * @author pizza(pizzamx@gmail.com)
 */
package subtitle.ui;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPartSite;

import subtitle.model.Moment;
import subtitle.model.SubtitleUnit;

public class AdjustTimelineDialog extends Dialog {
    private Text text_4;

    class SaveStateListener implements Listener {
        TimelineWidget widget;

        int index;

        public SaveStateListener(TimelineWidget w, int i) {
            widget = w;
            index = i;
        }

        public void handleEvent(Event event) {
            moments[index] = widget.getMoment();
        }

    }

    private Text text_2;

    private Text text;

    SubtitleUnit targetUnit;

    public TabFolder tabFolder;

    private TimelineWidget[] timelineWidgets;

    private Moment[] moments;

    public int index;

    List items;

    /**
     * Create the dialog
     * 
     * @param items
     * 
     * @param parentShell
     */
    public AdjustTimelineDialog(IWorkbenchPartSite site, SubtitleUnit unit, List items) {
        super( site );
        targetUnit = unit;
        this.items = items;
    }

    /**
     * Create contents of the dialog
     * 
     * @param parent
     */
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite)super.createDialogArea( parent );
        container.setLayout( new FormLayout() );
        timelineWidgets = new TimelineWidget[5];
        moments = new Moment[5];

        tabFolder = new TabFolder( container, SWT.NONE );
        final FormData formData_5 = new FormData();
        formData_5.bottom = new FormAttachment( 100, -5 );
        formData_5.right = new FormAttachment( 100, -5 );
        formData_5.top = new FormAttachment( 0, 0 );
        formData_5.left = new FormAttachment( 0, 5 );
        tabFolder.setLayoutData( formData_5 );

        tabFolder.addListener( SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                index = tabFolder.getSelectionIndex();
            }
        } );

        final TabItem tabItem = new TabItem( tabFolder, SWT.NONE );
        tabItem.setText( "����ѡ����" );

        final Composite composite = new Composite( tabFolder, SWT.NONE );
        tabItem.setControl( composite );

        timelineWidgets[0] = new TimelineWidget( composite, targetUnit.getStart() );
        timelineWidgets[0].setBounds( 24, 46, 225, 28 );

        timelineWidgets[1] = new TimelineWidget( composite, targetUnit.getEnd() );
        timelineWidgets[1].setBounds( 24, 97, 225, 28 );
        timelineWidgets[0].setNext( timelineWidgets[1] );
        timelineWidgets[1].setPrev( timelineWidgets[0] );

        Label label = new Label( composite, SWT.WRAP );
        label.setText( "���ҷ��������ֱ���ƶ����˸���������������ͬ��" );
        label.setBounds( 24, 144, 419, 76 );

        Label label_1 = new Label( composite, SWT.NONE );
        label_1.setText( "<--���п�ʼʱ��" );
        label_1.setBounds( 303, 46, 102, 20 );

        Label label_2 = new Label( composite, SWT.NONE );
        label_2.setText( "<--���н���ʱ��" );
        label_2.setBounds( 303, 97, 91, 20 );

        final TabItem tabItem_1 = new TabItem( tabFolder, SWT.NONE );
        tabItem_1.setText( "��ѡ���п�ʼ����" );

        final Composite composite_1 = new Composite( tabFolder, SWT.NONE );
        tabItem_1.setControl( composite_1 );

        timelineWidgets[2] = new TimelineWidget( composite_1, targetUnit.getStart() );
        timelineWidgets[2].setBounds( 24, 39, 225, 28 );

        final Label label_3 = new Label( composite_1, SWT.NONE );
        label_3.setText( "<--���п�ʼʱ��" );
        label_3.setBounds( 275, 45, 126, 13 );

        final Label label_4 = new Label( composite_1, SWT.WRAP );
        label_4.setText( "��ָ������֮��ο��еĿ�ʼʱ�䡣\n\n��ʱ��;�ʱ���ƫ��ֵ�����ӣ�����������ÿ����Ļ��" );
        label_4.setBounds( 24, 97, 437, 54 );

        final TabItem tabItem_2 = new TabItem( tabFolder, SWT.NONE );
        tabItem_2.setText( "ȫ������" );

        final Composite composite_2 = new Composite( tabFolder, SWT.NONE );
        tabItem_2.setControl( composite_2 );
        timelineWidgets[3] = new TimelineWidget( composite_2, ((SubtitleUnit)items.get( 0 )).getStart() );
        timelineWidgets[3].setBounds( 24, 46, 225, 28 );

        timelineWidgets[4] = new TimelineWidget( composite_2, ((SubtitleUnit)items.get( items.size() - 1 )).getStart() );
        timelineWidgets[4].setBounds( 24, 97, 225, 28 );
        timelineWidgets[3].setNext( timelineWidgets[4] );
        timelineWidgets[4].setPrev( timelineWidgets[3] );

        label_1 = new Label( composite_2, SWT.NONE );
        label_1.setText( "<--���п�ʼʱ��" );
        label_1.setBounds( 303, 46, 102, 20 );

        label_2 = new Label( composite_2, SWT.NONE );
        label_2.setText( "<--ĩ�н���ʱ��" );
        label_2.setBounds( 303, 97, 91, 20 );

        label = new Label( composite_2, SWT.WRAP );
        label.setText( "��ָ����ĩ�п�ʼʱ�䡣\n\n֮��ϵͳ����ԭ����ʱ����жԱȣ��ʵ��Ӻ�/��ǰÿ�е�ʱ����Э�����ࡣ" );
        label.setBounds( 24, 144, 419, 76 );

        tabFolder.addListener( SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                int index = tabFolder.getSelectionIndex();
                switch (index) {
                case 0:
                    timelineWidgets[0].forceFocus();
                    break;
                case 1:
                    timelineWidgets[2].forceFocus();
                    break;
                case 2:
                    timelineWidgets[3].forceFocus();
                    break;

                }
            }
        } );
        timelineWidgets[0].forceFocus();
        TimelineWidget widget;
        for (int i = 0; i < timelineWidgets.length; i++) {
            widget = timelineWidgets[i];
            // todo:�Ժ����ȥ����
            if (widget != null) {
                widget.addListener( SWT.Modify, new SaveStateListener( widget, i ) );
            }
        }
        return container;
    }

    /**
     * Create contents of the button bar
     * 
     * @param parent
     */
    protected void createButtonsForButtonBar(Composite parent) {
        createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true );
        createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );
    }

    /**
     * Return the initial size of the dialog
     */
    protected Point getInitialSize() {
        return new Point( 500, 375 );
    }
    protected void configureShell(Shell newShell) {
        super.configureShell( newShell );
        newShell.setText( "����ʱ����" );
    }
    public Moment getMoment(int index) {
        return moments[index];
    }
}
