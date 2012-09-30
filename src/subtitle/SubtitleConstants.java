package subtitle;

/**
 * Interface defining the application's command IDs. Key bindings can be defined
 * for specific commands. To associate an action with a command, use
 * IAction.setActionDefinitionId(commandId).
 * 
 * @see org.eclipse.jface.action.IAction#setActionDefinitionId(String)
 */
public class SubtitleConstants {

    public static final String CMD_OPEN_ORIGINAL_SUBTITLE = "subtitle.file.openOriginalSubtitle";

    public static final String CMD_MERGE = "subtitle.edit.merge";

    public static final String MENU_MAIN_POPUP = "subtitle.editor.popup";
}
