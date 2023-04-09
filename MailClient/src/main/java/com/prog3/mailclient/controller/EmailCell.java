package com.prog3.mailclient.controller;

import com.prog3.common.model.Email;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;
import javafx.scene.text.TextAlignment;

import java.time.format.DateTimeFormatter;

/**
 * This EmailCell class extends ListCell<Email> and is used to customize
 * the display of each cell in the email ListView. This class defines the
 * logic to display the content of the Email object inside the cell of the
 * ListView.
 */
class EmailCell extends ListCell<Email> {

    private final GridPane gridPane = new GridPane();
    private final Label senderLabel = new Label();
    private final Label subjectLabel = new Label();
    private final Label dateLabel = new Label();

    public EmailCell() {
        super();
        // Imposta l'allineamento della griglia
        gridPane.setAlignment(Pos.CENTER_LEFT);
        gridPane.setHgap(10);
        // Aggiungi le label alla griglia
        gridPane.add(senderLabel, 0, 0);
        gridPane.add(subjectLabel, 1, 0);
        gridPane.add(dateLabel, 2, 0);
        // Imposta il prefisso per la data
        dateLabel.setTextAlignment(TextAlignment.RIGHT);
        //dateLabel.setPrefWidth(100);
        senderLabel.setStyle("-fx-font-size: 14;");
        subjectLabel.setStyle("-fx-font-size: 14;");
        dateLabel.setStyle("-fx-font-size: 14;");
        // Aggiungi la griglia al componente
        setGraphic(gridPane);
    }


    /**
     * The updateItem function is called whenever the display of a cell needs to be updated.
     * If the cell is empty or if the item is null, the cell text is set to null.
     * Otherwise, the subject text of the email is set using item.getSubject().
     *
     * @param item  the Email object to display in the list cell
     * @param empty this parameter indicates whether the cell is empty or not
     */
    @Override
    protected void updateItem(Email item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            //setText(item.getSubject());
            // Imposta i valori delle label
            senderLabel.setText(item.getSender());
            subjectLabel.setText(item.getSubject());
            dateLabel.setText(item.getDate().format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yy")));
            // Aggiorna le dimensioni della griglia
            gridPane.setPrefWidth(getListView().getWidth() - 20);
            senderLabel.setPrefWidth(gridPane.getPrefWidth() * 0.25);
            subjectLabel.setPrefWidth(gridPane.getPrefWidth() * 0.50);
            setPrefHeight(34);
            setGraphic(gridPane);
        }
        // Aggiorna la ListView
        getListView().refresh();
    }
}

