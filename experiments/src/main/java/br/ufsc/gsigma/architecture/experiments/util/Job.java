package br.ufsc.gsigma.architecture.experiments.util;

public interface Job {
	public void run(int count, int total) throws Exception;
}
