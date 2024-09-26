import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {

    private final List<Document> allDocuments = new ArrayList<>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        if (document == null) {
            return null;
        }

        Optional<Document> getDocument = this.allDocuments.stream()
                .filter(newDocument -> newDocument.getAuthor().canEqual(document.getAuthor()) &&
                        newDocument.getContent().equals(document.getContent()))
                .findFirst();

        if (document.getId() == null || document.getId().isBlank()) {
            document.setId(UUID.randomUUID().toString());
        }

        if (getDocument.isPresent()) {
            Document existingDocument = getDocument.get();
            existingDocument.setId(document.getId());
            existingDocument.setTitle(document.getTitle());
            return existingDocument;
        } else {
            document.setCreated(Instant.now());
            this.allDocuments.add(document);
            return document;
        }
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        if (request == null) {
            return Collections.emptyList();
        }
        return this.allDocuments.stream()
                .filter(document -> request.getTitlePrefixes() == null ||
                        request.getTitlePrefixes().stream().anyMatch(prefixTitle -> document.getTitle().startsWith(prefixTitle)))
                .filter(document -> request.getContainsContents() == null ||
                        request.getContainsContents().stream().anyMatch(partOfContent -> document.getContent().contains(partOfContent)))
                .filter(document -> request.getAuthorIds() == null ||
                        request.getAuthorIds().stream().anyMatch(id -> document.getAuthor().getId().equals(id)))
                .filter(document -> request.getCreatedFrom() == null ||
                        document.getCreated().isAfter(request.getCreatedFrom()))
                .filter(document -> request.getCreatedTo() == null ||
                        document.getCreated().isBefore(request.getCreatedTo()))
                .toList();
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return this.allDocuments.stream()
                .filter(document -> document.getId().equals(id))
                .findFirst();
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}