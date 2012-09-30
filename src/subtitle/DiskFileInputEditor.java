/**
 * @author pizza(pizzamx@gmail.com)
 * ===============
 * In God We Trust
 * ===============
 * 2007-3-7 ÉÏÎç09:35:03
 *
 */
package subtitle;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IPersistableElement;

public class DiskFileInputEditor implements IPathEditorInput {
    private File reference, translation;

    public static final String ID = "subtitle.editor";

    public DiskFileInputEditor(File ref, File trans) {
        this.reference = ref;
        this.translation = trans;
    }

    public boolean exists() {
        return reference.exists();
    }

    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    public String getName() {
        return "getName()";
    }

    public String getToolTipText() {
        return "getToolTipText()";
    }

    public Object getAdapter(Class adapter) {
        return null;
    }

    public String getFactoryId() {
        return ID;
    }

    public void saveState(IMemento memento) {
        // ?
    }

    public File getReference() {
        return this.reference;
    }

    public IPath getPath() {
        return new Path( reference.getAbsolutePath() );
    }

    public IPersistableElement getPersistable() {
        return null;
    }

    public File getTranslation() {
        return translation;
    }

    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((reference == null) ? 0 : reference.hashCode());
        result = PRIME * result + ((translation == null) ? 0 : translation.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final DiskFileInputEditor other = (DiskFileInputEditor)obj;
        if (reference == null) {
            if (other.reference != null)
                return false;
        } else if (!reference.equals( other.reference ))
            return false;
        if (translation == null) {
            if (other.translation != null)
                return false;
        } else if (!translation.getAbsolutePath().equals( other.translation.getAbsolutePath() ))
            return false;
        return true;
    }

}
