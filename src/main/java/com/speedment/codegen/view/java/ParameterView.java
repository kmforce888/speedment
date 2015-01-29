package com.speedment.codegen.view.java;

import com.speedment.codegen.CodeGenerator;
import static com.speedment.codegen.CodeUtil.SPACE;
import com.speedment.codegen.model.parameter.Parameter_;
import com.speedment.codegen.view.CodeView;
import com.speedment.util.$;
import java.util.Optional;

/**
 *
 * @author Duncan
 */
public class ParameterView extends CodeView<Parameter_> {
	@Override
	public Optional<CharSequence> render(CodeGenerator renderer, Parameter_ model) {
		return Optional.of(new $(
			
			renderer.on(model.getType()).get(),
			SPACE,
			model.getName()
		));
	}
}