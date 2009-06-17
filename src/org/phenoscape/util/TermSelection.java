package org.phenoscape.util;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.obo.datamodel.OBOClass;

public class TermSelection implements Transferable {
    
    private final OBOClass term;
    public static DataFlavor termFlavor = createTermFlavor();

    public TermSelection(OBOClass term) {
        this.term = term;
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (this.isDataFlavorSupported(flavor)) {
            log().debug("Returning the term: " + this.term);
            if (flavor instanceof TermFlavor) {
                return this.term;
            } else if (flavor.equals(DataFlavor.stringFlavor)) {
                return this.term.getName();
            } else {
                return null;
            }
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }

    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] {termFlavor, DataFlavor.stringFlavor};
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        log().debug("Is flavor supported? " + flavor + ": " + (flavor instanceof TermFlavor));
        if (flavor instanceof TermFlavor) {
            return true;
        } else if (flavor.equals(DataFlavor.stringFlavor)) {
            return true;
        }
        return false;
    }
    
    private static DataFlavor createTermFlavor() {
        try {
            return new TermFlavor();
        } catch (ClassNotFoundException e) {
            log().error("Term class not found, couldn't create flavor for clipboard", e);
            throw new RuntimeException(e);
        }
    }
    
    public static class TermFlavor extends DataFlavor {
        
        public TermFlavor() throws ClassNotFoundException {
            super(DataFlavor.javaJVMLocalObjectMimeType + ";class=" + OBOClass.class.getName());
        }
    }

    private static Logger log() {
        return Logger.getLogger(TermSelection.class);
    }
}