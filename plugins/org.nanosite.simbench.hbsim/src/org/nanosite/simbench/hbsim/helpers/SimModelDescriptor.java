package org.nanosite.simbench.hbsim.helpers;

import org.nanosite.simbench.hbsim.FeatureConfig;
import org.nanosite.simbench.hbsim.Partitioning;
import org.nanosite.simbench.hbsim.Scenario;

public class SimModelDescriptor {

	Partitioning partitioning;
	Scenario scenario;
	FeatureConfig config;

	public SimModelDescriptor (Partitioning partitioning, Scenario scenario, FeatureConfig config)
	{
		this.partitioning = partitioning;
		this.scenario = scenario;
		this.config = config;
	}

	public boolean isValid() {
		return partitioning!=null && scenario!=null && config!=null;
	}

	public Partitioning getPartitioning() {
		return partitioning;
	}

	public Scenario getScenario() {
		return scenario;
	}

	public FeatureConfig getConfig() {
		return config;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((config == null) ? 0 : config.hashCode());
		result = prime * result
				+ ((partitioning == null) ? 0 : partitioning.hashCode());
		result = prime * result
				+ ((scenario == null) ? 0 : scenario.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimModelDescriptor other = (SimModelDescriptor) obj;
		if (config == null) {
			if (other.config != null)
				return false;
		} else if (!config.equals(other.config))
			return false;
		if (partitioning == null) {
			if (other.partitioning != null)
				return false;
		} else if (!partitioning.equals(other.partitioning))
			return false;
		if (scenario == null) {
			if (other.scenario != null)
				return false;
		} else if (!scenario.equals(other.scenario))
			return false;
		return true;
	}
}

