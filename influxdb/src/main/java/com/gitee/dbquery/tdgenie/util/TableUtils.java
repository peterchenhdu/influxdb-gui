package com.gitee.dbquery.tdgenie.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

public class TableUtils {

    /**
     * Install the keyboard handler:
     * + CTRL + C = copy to clipboard
     * + CTRL + V = paste to clipboard
     *
     * @param table
     */
    public static void installCopyPasteHandler(TableView<?> table) {

        // install copy/paste keyboard handler
        table.setOnKeyPressed(new TableKeyEventHandler());

    }

    /**
     * Copy/Paste keyboard event handler.
     * The handler uses the keyEvent's source for the clipboard data. The source must be of type TableView.
     */
    public static class TableKeyEventHandler implements EventHandler<KeyEvent> {

        KeyCodeCombination copyKeyCodeCompination = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY);
        KeyCodeCombination pasteKeyCodeCompination = new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_ANY);

        public void handle(final KeyEvent keyEvent) {

            if (copyKeyCodeCompination.match(keyEvent)) {

                if (keyEvent.getSource() instanceof TableView) {

                    // copy to clipboard
                    copySelectionToClipboard((TableView<?>) keyEvent.getSource());

                    // event is handled, consume it
                    keyEvent.consume();

                }

            } else if (pasteKeyCodeCompination.match(keyEvent)) {

                if (keyEvent.getSource() instanceof TableView) {

                    // copy to clipboard
                    pasteClipboard((TableView<?>) keyEvent.getSource());

                    // event is handled, consume it
                    keyEvent.consume();

                }

            }

        }

    }

    /**
     * Get table selection and copy it to the clipboard.
     *
     * @param table
     */
    public static void copySelectionToClipboard(TableView<?> table) {

        StringBuilder clipboardString = new StringBuilder();

        ObservableList<TablePosition> positionList = table.getSelectionModel().getSelectedCells();

        int prevRow = -1;

        for (TablePosition position : positionList) {

            int row = position.getRow();
            int col = position.getColumn();

            Object cell = (Object) table.getColumns().get(col).getCellData(row);

            // null-check: provide empty string for nulls
            if (cell == null) {
                cell = "";
            }

            // determine whether we advance in a row (tab) or a column
            // (newline).
            if (prevRow == row) {

                clipboardString.append('\t');

            } else if (prevRow != -1) {

                clipboardString.append('\n');

            }

            // create string from cell
            String text = cell.toString();

            // add new item to clipboard
            clipboardString.append(text);

            // remember previous
            prevRow = row;
        }

        // create clipboard content
        final ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(clipboardString.toString());

        // set clipboard content
        Clipboard.getSystemClipboard().setContent(clipboardContent);

        System.out.println("Clipboard string: " + clipboardContent);

    }

    public static void pasteClipboard(TableView<?> table) {

        TablePosition focusedCellPosition = table.getFocusModel().getFocusedCell();

        System.out.println("Pasting into cells starting at " + focusedCellPosition);

        String pasteString = Clipboard.getSystemClipboard().getString();

        System.out.println(pasteString);

        Pattern pattern = Pattern.compile("([^\t]*)\t([^\t]*)\t([^\n]*)(\n)?");
        Matcher matcher = pattern.matcher(pasteString);
        while (matcher.find()) {

            System.out.println(matcher.group(1) + "," + matcher.group(2) + "," + matcher.group(3));

            // TODO: what now? how to paste the data?

        }

    }


}
