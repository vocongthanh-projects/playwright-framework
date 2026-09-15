package vn.ots.qc.web.artifact;

import java.nio.file.Path;

public record EvidenceBundle(Path screenshot, Path html, Path trace) {
}
