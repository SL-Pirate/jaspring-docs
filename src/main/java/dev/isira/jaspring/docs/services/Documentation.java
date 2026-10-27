package dev.isira.jaspring.docs.services;

import dev.isira.jaspring.docs.models.DocPage;
import java.util.List;
import java.util.Optional;

/// The ordered documentation catalog. Content and navigation share the same source.
public interface Documentation {
    List<DocPage> pages();
    Optional<DocPage> find(String slug);
    List<DocPage> search(String query);
}
