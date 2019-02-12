package chocopy.common.codegen;

import java.util.Objects;

/**
 * A label in assembly.
 */
public class Label {

    /** The name of the label. */
    public final String labelName;

    /** A new label with name LABELNAME. */
    public Label(String labelName) {
        this.labelName = labelName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Label label = (Label) o;
        return Objects.equals(labelName, label.labelName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(labelName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return labelName;
    }
}
