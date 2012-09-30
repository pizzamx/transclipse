package subtitle;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;

import subtitle.utils.TimelineHelper;

import com.swtdesigner.SWTResourceManager;

public class View extends ViewPart {

	private StyledText styledText;

	private Table table;

	//	//翻译文本是否已改动？是的话要保存
	//	private boolean translationModified;

	//保存、退出、新开时检查
	private boolean translationFlag;

	//当前选中行号
	private int presentLineNumber;

	//当前行初始内容。因为只有一个编辑空间，故貌似无法使用ModifyListener
	private String presentLineInitial = "";

	private IEclipsePreferences preferences = new InstanceScope().getNode( "default" );

	public static final String ID = "subtitle.view";

	public void createPartControl(Composite parent) {
		Composite top = new Composite( parent, SWT.NONE );
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		top.setLayout( layout );

		table = new Table( top, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER );
		table.setLinesVisible( true );
		table.setHeaderVisible( true );
		final GridData gridData = new GridData( SWT.FILL, SWT.FILL, true, true );
		table.setLayoutData( gridData );

		final TableColumn col1 = new TableColumn( table, SWT.NONE );
		col1.setWidth( 50 );
		col1.setText( "行号" );

		final TableColumn col2 = new TableColumn( table, SWT.NONE );
		col2.setWidth( 200 );
		col2.setText( "时间轴" );

		final TableColumn col3 = new TableColumn( table, SWT.NONE );
		col3.setWidth( 250 );
		col3.setText( "原文" );

		final TableColumn col4 = new TableColumn( table, SWT.NONE );
		col4.setWidth( 250 );
		col4.setText( "译文" );

		table.addSelectionListener( new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//				System.out.println( preferences.getBoolean( "editing", false ) );
				lineFocused( table.indexOf( (TableItem) e.item ) );
			}
		} );
		table.addKeyListener( new KeyAdapter() {

			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.DEL) {
					int line = table.getSelectionIndex();
					TableItem item;
					for (int i = line + 1; i < table.getItemCount(); i++) {
						item = table.getItem( i );
						item.setText( i + "" );
					}
					table.remove( line );
				}
			}
		} );
		final Menu popup = new Menu( getSite().getShell(), SWT.POP_UP );
		final MenuItem mergeItem = new MenuItem( popup, SWT.PUSH );
		mergeItem.setText( "Merge" );
		table.setMenu( popup );
		mergeItem.addSelectionListener( new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				TableItem[] items = table.getSelection();
				TableItem item;
				if (items.length <= 1) {
					return;
				}
				StringBuffer orgi = new StringBuffer(), translated = new StringBuffer();
				int size, j;
				size = table.indexOf( items[items.length - 1] ) - table.indexOf( items[0] ) + 1;
				j = table.indexOf( items[0] );
				//merge
				for (int i = 0; i < size; i++) {
					item = table.getItem( j + i );
					orgi.append( item.getText( 2 ) );
					translated.append( item.getText( 3 ) );
				}
				//keep this one
				item = items[0];
				item.setText( 1, TimelineHelper.merge( TimelineHelper.split( item.getText( 1 ) )[0],
						TimelineHelper.split( table.getItem( j + size - 1 ).getText( 1 ) )[1] ) );
				item.setText( 2, orgi.toString() );
				item.setText( 3, translated.toString() );
				//remove
				for (int i = 1; i < size; i++) {
					table.remove( j + 1 );
				}
				//修改行号
				for (int i = j + 1; i < table.getItemCount(); i++) {
					item = table.getItem( i );
					item.setText( i + 1 + "" );
				}
			}
		} );

		styledText = new StyledText( top, SWT.V_SCROLL | SWT.BORDER | SWT.WRAP );
		//		styledText.setAlignment( SWT.CENTER );
		styledText.setFont( SWTResourceManager.getFont( "Verdana", 10, SWT.NONE ) );
		final GridData gridData_1 = new GridData( SWT.FILL, SWT.CENTER, false, false );
		gridData_1.heightHint = 30;
		gridData_1.widthHint = 395;
		styledText.setLayoutData( gridData_1 );

		styledText.addKeyListener( new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.stateMask == SWT.CTRL) {
					switch (e.keyCode) {
					case SWT.ARROW_UP:
						if (presentLineNumber > 0) {
							lineFocused( presentLineNumber - 1 );
						}
						break;
					case SWT.ARROW_DOWN:
						if (presentLineNumber < table.getItemCount() - 1) {
							lineFocused( presentLineNumber + 1 );
						}
						break;
					default:
						break;
					}
				}
			}

		} );

		// setup bold font
		Font boldFont = JFaceResources.getFontRegistry().getBold( JFaceResources.DEFAULT_FONT );
		// message contents
		initializeToolBar();

		try {
			readFile( "d:/grey.srt" );
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private void lineFocused(int lineNumber) {
		TableItem item;
		String lineInitial;
		if (!presentLineInitial.equals( styledText.getText() )) {
			item = table.getItem( presentLineNumber );
			item.setText( 3, styledText.getText() );
			translationFlag = true;
		}
		item = table.getItem( lineNumber );
		lineInitial = item.getText( 3 );
		//未曾翻译
		if (lineInitial == null || "".equals( lineInitial.trim() )) {
			//			lineInitial = item.getText( 2 );
			styledText.setText( "" );
		} else {
			styledText.setText( lineInitial );
		}
		presentLineNumber = lineNumber;
		presentLineInitial = lineInitial;
		table.select( lineNumber );
		if (preferences.getBoolean( "editing", false )) {
			table.setSelection( lineNumber );
			styledText.setFocus();
			styledText.setCaretOffset( styledText.getText().length() );
		}
	}

	public void readFile(String filename) throws IOException {

		table.removeAll();

		File file = new File( filename );
		//		TableItem t = new TableItem( table, SWT.NONE );
		//		t.setText( new String[] { "11", "22", "33" } );
		BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream( file ), Charset
				.forName( "GBK" ) ) );
		String line;
		String[] itemText;
		TableItem item;
		while ( ( line = reader.readLine() ) != null ) {
			int field = 1;
			itemText = new String[3];
			while ( !( "".equals( line.trim() ) ) ) {
				switch (field) {
				case 1:
					//行号
					itemText[0] = line;
					break;
				case 2:
					//时间轴
					itemText[1] = line;
					break;
				default:
					//正文
					itemText[2] = itemText[2] == null ? line : new StringBuffer( itemText[2] ).append( " ↓ " )
							.append( line ).toString();
					break;
				}
				if (( line = reader.readLine() ) != null) {
					field++;
				}
			}
			item = new TableItem( table, SWT.NONE );
			item.setText( itemText );
		}
		lineFocused( 0 );
		reader.close();
	}

	private void initializeToolBar() {
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
	}

	public void setFocus() {
	}

	public void saveFile(String fileName) throws IOException {
		File file = new File( fileName );
		BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( file ) ) );
		TableItem item;
		for (int i = 0; i < table.getItemCount(); i++) {
			item = table.getItem( i );
			writer.write( item.getText( 0 ) + "\n" );
			writer.write( item.getText( 1 ) + "\n" );
			writer.write( item.getText( 3 ) + "\n\n" );
		}
		writer.close();
		new MessageDialog( getSite().getShell(), "保存翻译", null, "翻译已经成功保存到\n" + fileName,
				MessageDialog.INFORMATION, new String[] { "知道了" }, 0 ).open();
	}
}
