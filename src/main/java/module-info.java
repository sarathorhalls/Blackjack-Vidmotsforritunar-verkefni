module hi.verkefni {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.tinylog.api;

    opens hi.verkefni.vidmot to javafx.fxml;

    exports hi.verkefni.vidmot;
}
