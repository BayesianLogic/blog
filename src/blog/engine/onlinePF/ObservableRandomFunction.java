package blog.engine.onlinePF;

import java.util.List;

import blog.model.DependencyModel;
import blog.model.RandomFunction;
import blog.model.Type;

public class ObservableRandomFunction extends RandomFunction{

	public ObservableRandomFunction(String fname, List arg_types,
			Type ret_type, DependencyModel depmodel) {
		super(fname, arg_types, ret_type, depmodel);
	}

}
