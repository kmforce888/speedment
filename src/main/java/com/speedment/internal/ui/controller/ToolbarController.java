/**
 *
 * Copyright (c) 2006-2016, Speedment, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); You may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.speedment.internal.ui.controller;
import com.speedment.component.brand.Brand;
import com.speedment.internal.ui.UISession;
import com.speedment.internal.ui.util.Loader;
import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import java.net.URL;
import static java.util.Objects.requireNonNull;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author Emil Forslund
 */
public final class ToolbarController implements Initializable {
    
    public final static String ICON_SIZE = "2em";
    private final UISession session;
    
    private @FXML Button buttonReload;
    private @FXML Button buttonGenerate;
    private @FXML ImageView brand;
    
    private ToolbarController(UISession session) {
        this.session = requireNonNull(session);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        buttonReload.setOnAction(session.reload());
        buttonGenerate.setOnAction(session.generate());
        
        buttonReload.setGraphic(GlyphsDude.createIcon(FontAwesomeIcon.REFRESH, ICON_SIZE));
        buttonGenerate.setGraphic(GlyphsDude.createIcon(FontAwesomeIcon.PLAY_CIRCLE, ICON_SIZE));

        final Brand uiBrand = session.getSpeedment().getUserInterfaceComponent().getBrand();
        uiBrand.logoLarge()
            .map(Image::new)
            .ifPresent(brand::setImage);
    }
    
    public static Node create(UISession session) {
        return Loader.create(session, "Toolbar");
	}
}