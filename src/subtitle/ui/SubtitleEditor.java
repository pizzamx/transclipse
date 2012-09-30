/**
 * @author pizza(pizzamx@gmail.com)
 * ===============
 * In God We Trust
 * ===============
 * 2007-3-3 下午06:58:44
 *
 */
package subtitle.ui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.ObjectUndoContext;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.commands.operations.UndoContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.operations.OperationHistoryActionHandler;
import org.eclipse.ui.operations.RedoActionHandler;
import org.eclipse.ui.operations.UndoActionHandler;
import org.eclipse.ui.part.EditorPart;

import subtitle.DiskFileInputEditor;
import subtitle.SubtitleConstants;
import subtitle.model.SubtitleUnit;
import subtitle.utils.SubtitleReader;

import com.swtdesigner.SWTResourceManager;

public class SubtitleEditor extends EditorPart implements ISelectionProvider, IOperationHistoryListener {

    public static final String ID = "subtitle.Editor"; //$NON-NLS-1$

    public final static int PROP_UNDOABLE_OP_PERFORMED = 0x00011;

    IOperationHistory operationHistory;

    TableViewer tableViewer;

    // 保存、退出、新开时检查
    private boolean dirty;

    // private TextViewer textArea;

    private List items;

    // 忘了这是啥了...
    private List operationList;

    private IEclipsePreferences preferences = new InstanceScope().getNode( "default" );

    // //翻译文本是否已改动？是的话要保存
    // private boolean translationModified;

    // 当前行初始内容。因为只有一个编辑空间，故貌似无法使用ModifyListener
    private String presentLineInitial = "";

    // 当前选中行号
    private int presentLineNumber;

    private Map registry = new HashMap();

    private Table table;

    private StyledText originalBox, translationBox;

    // 本意是标记是否在执行翻译框的undo操作
    // 只要是修改翻译框而不想写入operation history的操作都必须将其赋为true，改完了再将其改为false
    // 无需sync
    // 参考自http://www.blogjava.net/qujinlong123/archive/2007/06/07/122520.html
    private boolean undoingTranslation;

    // 记录翻译框的undo操作
    Stack editStack = new Stack();

    // 翻译后的文件
    private String transFileName, refFileName;

    private IUndoContext undoContext, editContext = new UndoContext();

    public SubtitleEditor() {
        operationList = new ArrayList();
    }

    /**
     * Create contents of the editor part
     * 
     * @param parent
     */
    public void createPartControl(Composite parent) {
        System.currentTimeMillis();
        Composite top = new Composite( parent, SWT.NONE );
        top.setLayout( new FillLayout() );

        SashForm sf = new SashForm( top, SWT.VERTICAL );
        tableViewer = new TableViewer( sf, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION );

        table = tableViewer.getTable();
        table.setSize( -1, 500 );
        table.setFont( SWTResourceManager.getFont( "Verdana", 10, SWT.NONE ) );
        table.setLinesVisible( true );
        table.setHeaderVisible( true );

        final TableColumn col1 = new TableColumn( table, SWT.NONE );
        col1.setWidth( 50 );
        col1.setText( "行号" );
        final TableColumn col2 = new TableColumn( table, SWT.NONE );
        col2.setWidth( 120 );
        col2.setText( "时间轴" );
        final TableColumn col3 = new TableColumn( table, SWT.NONE );
        col3.setWidth( 443 );
        col3.setText( "原文" );
        final TableColumn col4 = new TableColumn( table, SWT.NONE );
        col4.setWidth( 473 );
        col4.setText( "译文" );

        tableViewer.setContentProvider( new IStructuredContentProvider() {

            public void dispose() {
            }

            public Object[] getElements(Object inputElement) {
                return ((List)inputElement).toArray();
            }

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }
        } );
        class SrcTableLableProvider implements ITableLabelProvider, ITableColorProvider {

            public void addListener(ILabelProviderListener listener) {
            }

            public void dispose() {
            }

            public Color getBackground(Object element, int columnIndex) {
                SubtitleUnit unit = (SubtitleUnit)element;
                String t = unit.getTranslation();
                if (t == null || "".equals( t.trim() )) {
                    return null;
                }
                String[] strings = t.split( SubtitleReader.NEW_LINE );
                for (int i = 0; i < strings.length; i++) {
                    if (strings[i].length() > 18) {
                        return SubtitleEditor.this.getEditorSite().getShell().getDisplay().getSystemColor( SWT.COLOR_GRAY );
                    }
                }
                return null;
            }

            public Image getColumnImage(Object element, int columnIndex) {
                return null;
            }

            public String getColumnText(Object element, int columnIndex) {
                SubtitleUnit unit = (SubtitleUnit)element;
                switch (columnIndex) {
                case 0:
                    return items.indexOf( element ) + 1 + "";
                case 1:
                    return unit.getShortTimeline();
                case 2:
                    return unit.getOriginal() == null ? "" : unit.getOriginal().replaceAll( SubtitleReader.NEW_LINE, " ↓ " );
                case 3:
                    return unit.getTranslation().replaceAll( SubtitleReader.NEW_LINE, " ↓ " );
                default:
                    return "";
                }
            }

            public Color getForeground(Object element, int columnIndex) {
                return null;
            }

            public boolean isLabelProperty(Object element, String property) {
                return false;
            }

            public void removeListener(ILabelProviderListener listener) {
            }
        }
        tableViewer.setLabelProvider( new SrcTableLableProvider() );
        tableViewer.setInput( items );

        table.addKeyListener( new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == SWT.DEL) {
                    removePrecentLine();
                }
            }
        } );
        table.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                ISelection selection = tableViewer.getSelection();
                List selectedLines = ((IStructuredSelection)selection).toList();
                if (!selectedLines.isEmpty()) {
                    // 多选的话如果再focus就多选不了了（病句？）
                    if (selectedLines.size() == 1) {
                        lineFocused( items.indexOf( selectedLines.get( 0 ) ) );
                    }
                }
            }
        } );

        // table.setSelection( 0 );
        // TODO:如何强制获得焦点？
        getSite().setSelectionProvider( tableViewer );

        SashForm sf2 = new SashForm( sf, SWT.HORIZONTAL );
        sf.setWeights( new int[] { 75, 25 } );
        originalBox = new StyledText( sf2, SWT.WRAP | SWT.READ_ONLY );
        originalBox.setAlignment( SWT.CENTER );
        originalBox.setFont( SWTResourceManager.getFont( "Lucida Console", 11, SWT.NONE ) );

        translationBox = new StyledText( sf2, SWT.NONE );
        // textArea = new TextViewer( top, SWT.BORDER );
        // text = textArea.getTextWidget();
        translationBox.setAlignment( SWT.CENTER );
        // textArea.setDocument( new Document() );
        translationBox.setFont( SWTResourceManager.getFont( "Verdana", 10, SWT.NONE ) );

        translationBox.addKeyListener( new KeyAdapter() {
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
                    case SWT.ARROW_RIGHT:
                        copyOriginal( presentLineNumber );
                        break;
                    case SWT.DEL:
                        removePrecentLine();
                        break;
                    default:
                        break;
                    }
                }
            }

        } );
        translationBox.addExtendedModifyListener( new ExtendedModifyListener() {
            public void modifyText(final ExtendedModifyEvent event) {
                if (undoingTranslation) {
                    return;
                }
                setDirty( true, new AbstractOperation( "编辑译文" ) {
                    int start = event.start;

                    String newText = translationBox.getText().substring( event.start, event.start + event.length );

                    String replacedText = event.replacedText;

                    public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
                        return Status.OK_STATUS;
                    }

                    public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
                        undoingTranslation = true;
                        translationBox.replaceTextRange( start, replacedText.length(), newText );
                        // translationBox.setCaretOffset(
                        // translationBox.getCaretOffset() +
                        // replacedText.length() );

                        // 换行之后就要退栈~

                        undoingTranslation = false;
                        return Status.OK_STATUS;
                    }

                    public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
                        undoingTranslation = true;
                        translationBox.replaceTextRange( start, newText.length(), replacedText );
                        undoingTranslation = false;
                        return Status.OK_STATUS;
                    }
                }, editContext );
            }
        } );
        sf2.setWeights( new int[] { 65, 35 } );

        MenuManager menuMgr = new MenuManager();
        menuMgr.add( new GroupMarker( IWorkbenchActionConstants.MB_ADDITIONS ) );
        Menu menu = menuMgr.createContextMenu( table );
        table.setMenu( menu );
        getSite().registerContextMenu( SubtitleConstants.MENU_MAIN_POPUP, menuMgr, this );

        OperationHistoryActionHandler undoActionHandler = new UndoActionHandler( getSite(), undoContext ), redoActionHandler = new RedoActionHandler( getSite(),
                undoContext );
        registry.put( ActionFactory.UNDO.getId(), undoActionHandler );
        registry.put( ActionFactory.REDO.getId(), redoActionHandler );

    }

    public void doSave(IProgressMonitor monitor) {
        // Do the Save operation
        try {
            if (transFileName == null) {
                FileDialog fd = new FileDialog( getEditorSite().getShell(), SWT.SAVE );
                fd.setText( "保存翻译" );
                fd.setFilterExtensions( new String[] { "*.srt" } );
                transFileName = fd.open();
            }
            if (transFileName != null) {
                File translation = new File( transFileName ), reference = new File( refFileName );
                BufferedWriter refWriter = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( reference ) ) );
                BufferedWriter transWriter = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( translation ) ) );
                SubtitleUnit unit;
                String o, t;
                for (int i = 0; i < items.size(); i++) {
                    unit = (SubtitleUnit)items.get( i );
                    transWriter.write( i + 1 + SubtitleReader.NEW_LINE );
                    refWriter.write( i + 1 + SubtitleReader.NEW_LINE );
                    transWriter.write( unit.getTimeline() + SubtitleReader.NEW_LINE );
                    refWriter.write( unit.getTimeline() + SubtitleReader.NEW_LINE );
                    t = unit.getTranslation();
                    o = unit.getOriginal();
                    if(o == null) {
                        o = "";
                    }
                    // if (t == null || "".equals( t.trim() )) {
                    // t = unit.getOriginal();
                    // }
                    refWriter.write( o + SubtitleReader.NEW_LINE + SubtitleReader.NEW_LINE );
                    transWriter.write( t + SubtitleReader.NEW_LINE + SubtitleReader.NEW_LINE );
                }
                transWriter.close();
                refWriter.close();
                setDirty( false, null );
                firePropertyChange( PROP_DIRTY );
                // MessageDialog.openInformation( getSite().getShell(), "保存翻译",
                // "翻译已经成功保存到\r\n" + fileName );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void doSaveAs() {
    }
    public Map getRegistry() {
        return registry;
    }

    public List getOperationList() {
        return operationList;
    }

    public void init(IEditorSite site, IEditorInput input) {
        setSite( site );
        setInput( input );
        operationHistory = site.getWorkbenchWindow().getWorkbench().getOperationSupport().getOperationHistory();
        operationHistory.addOperationHistoryListener( this );
        undoContext = createUndoContext();
        final DiskFileInputEditor inputEditor = (DiskFileInputEditor)input;
        final List reference, translation;
        try {
            reference = readFile( inputEditor.getReference() );
            translation = readFile( inputEditor.getTranslation() );
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        try {
            getSite().getWorkbenchWindow().getWorkbench().getProgressService().run( true, true, new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    monitor.beginTask( "加载字幕文件:", reference.size() );
                    // size undetermined. cannot apply Math.max either.
                    items = new LinkedList();
                    int i = 0, j = 0;
                    SubtitleUnit ref, trans, copy;
                    // 其实这里还少了一步排序……不过我们应该认为99.9%的情况下是正序的
                    while (i < reference.size() && j < translation.size()) {
                        ref = (SubtitleUnit)reference.get( i );
                        trans = (SubtitleUnit)translation.get( j );
                        copy = new SubtitleUnit();
                        if (ref.getStart().equals( trans.getStart() ) && ref.getEnd().equals( trans.getEnd() )) {
                            copy.setTimeline( ref.getTimeline() );
                            copy.setOriginal( ref.getOriginal() );
                            copy.setTranslation( trans.getOriginal() );
                            items.add( copy );
                            i++;
                            j++;
                        } else {
                            if (ref.getStart().compareTo( trans.getStart() ) < 0) {
                                //有原文没有翻译
                                copy.setTimeline( ref.getTimeline() );
                                copy.setOriginal( ref.getOriginal() );
                                items.add( copy );
                                i++;
                            } else {
                                //有有翻译没有原文
                                copy.setTimeline( trans.getTimeline() );
                                copy.setTranslation( trans.getOriginal() );
                                items.add( copy );
                                j++;
                            }
                        }
                        monitor.subTask( "还剩下 " + (reference.size() - 1 - i) + " 行" );
                        monitor.worked( i + 1 );
                    }
                    while (i < reference.size()) {
                        ref = (SubtitleUnit)reference.get( i++ );
                        copy = new SubtitleUnit();
                        copy.setTimeline( ref.getTimeline() );
                        copy.setOriginal( ref.getOriginal() );
                        items.add( copy );
                    }
                    while (j < translation.size()) {
                        trans = (SubtitleUnit)translation.get( j++ );
                        copy = new SubtitleUnit();
                        copy.setTimeline( trans.getTimeline() );
                        copy.setTranslation( trans.getOriginal() );
                        items.add( copy );
                    }
                    monitor.done();
                    transFileName = inputEditor.getTranslation().getAbsolutePath();
                    refFileName = inputEditor.getReference().getAbsolutePath();
                    setPartName( inputEditor.getTranslation().getName() );
                    new Thread() {
                        public void run() {
                            while (true) {
                                try {
                                    Thread.sleep( 10 * 1000 );
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                // todo:退出会导致空指针异常
                                if (transFileName != null && PlatformUI.isWorkbenchRunning()) {
                                    getSite().getShell().getDisplay().asyncExec( new Runnable() {
                                        public void run() {
                                            // todo:自动保存和undo的关系比较难处理啊
                                            // getSite().getPage().saveEditor(
                                            // SubtitleEditor.this, false );
                                        }
                                    } );
                                }
                            }
                        }
                    }.start();
                }
            } );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Initialize the editor part

    private IUndoContext createUndoContext() {
        undoContext = new ObjectUndoContext( this, "[undocontext]" + transFileName );
        return undoContext;
    }

    public boolean isDirty() {
        return dirty;
    }

    public boolean isSaveAsAllowed() {
        return false;
    }

    public void openTranslation(DiskFileInputEditor editor) {
    }

    public List readFile(final File file) throws IOException {
        SubtitleReader reader = new SubtitleReader( file );
        return reader.read();
    }
    public void setFocus() {
        // Set the focus
    }
    private void copyOriginal(final int presentLineNumber) {
        final SubtitleUnit unit = (SubtitleUnit)items.get( presentLineNumber );
        setDirty( true, new AbstractOperation( "复制原文" ) {

            public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
                translationBox.setText( unit.getOriginal() );
                // unit.setTranslation( unit.getOriginal() );
                // text.setText( unit.getOriginal() );
                // text.setCaretOffset( text.getText().length() );
                // tableViewer.refresh( unit );
                return Status.OK_STATUS;
            }

            public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
                return execute( monitor, info );
            }

            public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
                // SubtitleUnit unit = (SubtitleUnit)items.get(
                // presentLineNumber );
                // unit.setTranslation( "" );
                // tableViewer.refresh( unit );
                translationBox.setText( "" );
                return Status.OK_STATUS;
            }
        } );
    }

    public void lineFocused(int lineNumber) {
        lineFocused( lineNumber, true );
    }
    private void lineFocused(int lineNumber, boolean shouldFocus) {
        String lineInitial;
        // 如果修改了
        if (!presentLineInitial.equals( translationBox.getText() )) {
            final SubtitleUnit lastUnit = (SubtitleUnit)items.get( presentLineNumber );
            final String lastTranString = lastUnit.getTranslation(), newString = translationBox.getText();
            setDirty( true, new AbstractOperation( "修改" ) {
                public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
                    lastUnit.setTranslation( newString );
                    // tableViewer.refresh( lastUnit );
                    // 这里如果不是null该是啥？
                    tableViewer.update( lastUnit, null );
                    return Status.OK_STATUS;
                }

                public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
                    return execute( monitor, info );
                }

                public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
                    lastUnit.setTranslation( lastTranString );
                    tableViewer.update( lastUnit, null );
                    return Status.OK_STATUS;
                }
            } );
        }
        if (lineNumber >= 0 && lineNumber < items.size()) {
            SubtitleUnit unit = (SubtitleUnit)items.get( lineNumber );
            lineInitial = unit.getTranslation();
            showOriginal( lineNumber );
            // 未曾翻译
            undoingTranslation = true;
            if (lineInitial == null || "".equals( lineInitial.trim() )) {
                // lineInitial = item.getText( 2 );
                translationBox.setText( "" );
            } else {
                translationBox.setText( lineInitial );
            }
            undoingTranslation = false;

            presentLineNumber = lineNumber;
            presentLineInitial = lineInitial;
            if (shouldFocus) {
                table.setSelection( lineNumber );
            }
            translationBox.setCaretOffset( translationBox.getText().length() );

            // FIXME: 把编辑undo操作全部移除……好傻啊，怎么才能更gay一点呢？
            while (!editStack.empty()) {
                operationHistory.replaceOperation( ((IUndoableOperation)editStack.pop()), new IUndoableOperation[] {} );
            }
            // if (preferences.getBoolean( "editing", false )) {
            // styledText.setFocus();
            // }
        }
    }

    private void operationPerformed(IUndoableOperation operation, IUndoContext context) {
        if (context != null) {
            // operation.addContext( context );//好像没用
            if (context == editContext) {
                editStack.push( operation );
            }
        }
        operation.addContext( undoContext );
        try {
            operationHistory.execute( operation, null, null );
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void removePrecentLine() {
        ISelection selection = tableViewer.getSelection();
        final List selectedLines = ((IStructuredSelection)selection).toList();
        final int[] indices = new int[selectedLines.size()];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = items.indexOf( selectedLines.get( i ) );
        }
        setDirty( true, new AbstractOperation( "删除" ) {
            public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
                items.removeAll( selectedLines );
                tableViewer.setInput( items );
                table.setSelection( indices[0] );
                lineFocused( indices[0] );
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

    public TableViewer getTableViewer() {
        return tableViewer;
    }

    public void setDirty(boolean dirty, IUndoableOperation operation, IUndoContext context) {
        this.dirty = dirty;
        if (operation != null) {
            operationPerformed( operation, context );
            // todo:dirty标记应该做出相应
        }
        firePropertyChange( PROP_DIRTY );
    }

    public void setDirty(boolean dirty, IUndoableOperation operation) {
        this.dirty = dirty;
        if (operation != null) {
            operationPerformed( operation, null );
        }
        firePropertyChange( PROP_DIRTY );
    }

    private void showOriginal(int lineNumber) {
        SubtitleUnit unit = (SubtitleUnit)items.get( lineNumber );
        String oriString = unit.getOriginal();
        if (oriString.startsWith( "- " )) {
            // 对话需要靠左对齐
            String[] lines = oriString.split( SubtitleReader.NEW_LINE );
            int max = 0;
            for (int i = 0; i < lines.length; i++) {
                max = Math.max( max, lines[i].length() );
            }
            StringBuffer buffer = new StringBuffer( lines.length * max );
            for (int i = 0; i < lines.length; i++) {
                boolean isWrappedLine = false;
                // 对话也可能多行
                if (!lines[i].startsWith( "- " )) {
                    buffer.append( "  " );
                    isWrappedLine = true;
                }
                buffer.append( lines[i] );
                int padding = max - lines[i].length() - (isWrappedLine ? 2 : 0);
                for (int j = 0; j < padding; j++) {
                    buffer.append( ' ' );
                }
                buffer.append( SubtitleReader.NEW_LINE );
            }
            buffer.deleteCharAt( buffer.length() - 2 );
            originalBox.setText( unit.getTimeline() + SubtitleReader.NEW_LINE + buffer.toString() );
        } else {
            originalBox.setText( unit.getTimeline() + SubtitleReader.NEW_LINE + oriString );
        }
        int timelineLength = unit.getTimeline().length();
        StyleRange range = new StyleRange();
        range.start = 0;
        range.length = timelineLength;
        range.foreground = ColorConstants.gray;
        originalBox.setStyleRange( range );
    }

    public Table getTable() {
        return table;
    }

    public void addSelectionChangedListener(ISelectionChangedListener listener) {
    }

    public ISelection getSelection() {
        return null;
    }

    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
    }

    public void setSelection(ISelection selection) {
    }

    public List getItems() {
        return items;
    }

    public void historyNotification(OperationHistoryEvent event) {
        IOperationHistory history = event.getHistory();
        setDirty( history.canUndo( undoContext ), null );
    }
}
