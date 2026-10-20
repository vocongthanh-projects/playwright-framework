package vn.ots.qc.web.locator;

import java.util.List;

/** Application-owned logical locator contract consumed by the framework. */
public interface LocatorDefinition {
    String logicalName();

    List<LocatorCandidate> candidates();
}
