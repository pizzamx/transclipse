/**
 * Created on 2007-5-21
 * @author pizza(pizzamx@gmail.com)
 */
package subtitle.ui;

import java.text.NumberFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import subtitle.model.Moment;

import com.swtdesigner.SWTResourceManager;

public class TimelineWidget extends Canvas {
    class KeySensor extends KeyAdapter implements VerifyListener {

        Text text;

        int index;

        public KeySensor(Text t, int i) {
            text = t;
            index = i;
        }

        public void verifyText(VerifyEvent e) {
        }
        public void keyPressed(KeyEvent e) {
            if (e.keyCode == SWT.BS || e.keyCode == SWT.ARROW_LEFT) {
                if (index > 0) {
                    texts[index - 1].forceFocus();
                } else if (index == 0 && prev != null) {
                    prev.texts[8].forceFocus();
                }
            }
            if (e.character >= '0' && e.character <= '9' || e.keyCode == SWT.ARROW_RIGHT) {
                if (index < 8) {
                    texts[index + 1].forceFocus();
                } else if (index == 8 && next != null) {
                    next.texts[0].forceFocus();
                } else {
                    text.selectAll();
                }
            } else {
                e.doit = false;
            }
        }
    }

    class FocusSensor extends FocusAdapter {
        Text text;

        public FocusSensor(Text t) {
            text = t;
        }

        public void focusGained(FocusEvent e) {
            text.selectAll();
        }
    }

    class ModifyListener implements Listener {
        public void handleEvent(Event event) {
            notifyListeners( SWT.Modify, event );
        }
    }

    Text[] texts;

    TimelineWidget next, prev;

    public TimelineWidget(Composite parent, Moment moment) {
        super( parent, SWT.BORDER );
        RowLayout rowLayout = new RowLayout();
        // rowLayout.wrap = false;
        rowLayout.fill = true;
        // rowLayout.marginHeight = 1;
        // rowLayout.marginWidth = 1;
        // rowLayout.marginLeft = 1;
        // rowLayout.marginTop = 1;
        // rowLayout.spacing = 1;
        setLayout( rowLayout );
        RowData data = new RowData( 15, 18 );

        texts = new Text[9];
        texts[0] = new Text( this, SWT.FLAT );
        texts[1] = new Text( this, SWT.FLAT );

        Label label = new Label( this, SWT.CENTER );
        label.setText( " : " );

        texts[2] = new Text( this, SWT.FLAT );
        texts[3] = new Text( this, SWT.FLAT );

        label = new Label( this, SWT.CENTER );
        label.setText( " : " );

        texts[4] = new Text( this, SWT.FLAT );
        texts[5] = new Text( this, SWT.FLAT );

        label = new Label( this, SWT.CENTER );
        label.setText( " , " );

        texts[6] = new Text( this, SWT.FLAT );
        texts[7] = new Text( this, SWT.FLAT );
        texts[8] = new Text( this, SWT.FLAT );

        Text text;
        KeySensor sensor;
        ModifyListener modifyListener = new ModifyListener();
        for (int i = 0; i < texts.length; i++) {
            text = texts[i];
            text.setLayoutData( data );
            text.setFont( SWTResourceManager.getFont( "Verdana", 12, SWT.NONE ) );
            sensor = new KeySensor( text, i );
            text.addVerifyListener( sensor );
            text.addKeyListener( sensor );
            text.addFocusListener( new FocusSensor( text ) );
            text.addListener( SWT.Modify, modifyListener );
        }

        if (moment != null) {
            int[] values = new int[] { moment.getHour(), moment.getMin(), moment.getSecond(), moment.getMillisecond() };
            NumberFormat formatter = NumberFormat.getInstance();
            formatter.setMinimumIntegerDigits( 2 );
            String string = formatter.format( values[0] );
            texts[0].setText( string.substring( 0, 1 ) );
            texts[1].setText( string.substring( 1 ) );

            string = formatter.format( values[1] );
            texts[2].setText( string.substring( 0, 1 ) );
            texts[3].setText( string.substring( 1 ) );

            string = formatter.format( values[2] );
            texts[4].setText( string.substring( 0, 1 ) );
            texts[5].setText( string.substring( 1 ) );

            formatter.setMinimumIntegerDigits( 3 );
            string = formatter.format( values[3] );
            texts[6].setText( string.substring( 0, 1 ) );
            texts[7].setText( string.substring( 1, 2 ) );
            texts[8].setText( string.substring( 2 ) );
        }

        setSize( 225, 28 );

    }
    public boolean forceFocus() {
        return texts[0].forceFocus();
    }
    public TimelineWidget(Composite parent) {
        this( parent, null );
    }
    public static void main(String[] args) {
        final Display display = new Display();
        final Shell shell = new Shell( display );
        Composite parent = new Composite( shell, SWT.NONE );
        Composite test = new TimelineWidget( parent, new Moment( "00:07:17,033" ) );
        parent.pack();
         shell.pack();
        shell.open();
        test.forceFocus();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }
    public void setNext(TimelineWidget widget) {
        next = widget;
    }
    public void setPrev(TimelineWidget prev) {
        this.prev = prev;
    }
    public Moment getMoment() {
        return new Moment( Integer.parseInt( texts[0].getText() + texts[1].getText() ), Integer.parseInt( texts[2].getText() + texts[3].getText() ), Integer
                .parseInt( texts[4].getText() + texts[5].getText() ), Integer.parseInt( texts[6].getText() + texts[7].getText() + texts[8].getText() ) );
    }

}
