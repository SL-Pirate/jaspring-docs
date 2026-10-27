package dev.isira.jaspring.docs.models;

import java.util.List;

public record DocPage(String slug, String group, String title, String description, String source,
                      List<Section> sections) {
    public DocPage {
        sections = List.copyOf(sections);
    }

    public record Section(String id, String title, List<String> paragraphs, String language,
                          String filename, String code, String note) {
        public Section {
            paragraphs = List.copyOf(paragraphs);
        }
    }
}
