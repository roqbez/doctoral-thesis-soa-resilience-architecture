package br.ufsc.gsigma.infrastructure.util.lpsolve;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;
import br.ufsc.gsigma.infrastructure.util.JniLibrary;

public class LpSolveHelper {

	static {
		new JniLibrary("lpsolve55").load();
		new JniLibrary("lpsolve55j").load();
	}

	public static LpSolve makeLp(int rows, int columns) throws LpSolveException {
		return LpSolve.makeLp(rows, columns);
	}

}
