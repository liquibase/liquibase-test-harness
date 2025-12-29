package liquibase.harness.config

import liquibase.resource.Resource
import liquibase.resource.ResourceAccessor

/**
 * A ResourceAccessor wrapper that returns only the first match from the delegate.
 *
 * This solves the classpath duplicate problem where both local test-classes and JAR
 * contain the same resource path. Instead of returning all matches (which causes
 * Liquibase duplicate file errors), this returns only the first match.
 *
 * When Maven runs tests, classpath ordering is:
 * 1. target/test-classes (local project overrides)
 * 2. Dependencies (including test-harness JAR defaults)
 *
 * So the first match will be the local override if it exists, otherwise the JAR default.
 *
 * Only overrides getAll() since both get() and the deprecated openStreams() use it internally.
 */
class FirstMatchResourceAccessor implements ResourceAccessor {

    private final ResourceAccessor delegate

    FirstMatchResourceAccessor(ResourceAccessor delegate) {
        this.delegate = delegate
    }

    @Override
    List<Resource> getAll(String path) throws IOException {
        List<Resource> resources = delegate.getAll(path)
        if (resources == null || resources.isEmpty()) {
            return resources
        }
        // Return only the first resource - allows local overrides to win
        return [resources.first()]
    }

    @Override
    List<Resource> search(String path, boolean recursive) throws IOException {
        // Search doesn't need first-match behavior - it's for discovery
        return delegate.search(path, recursive)
    }

    @Override
    List<String> describeLocations() {
        return delegate.describeLocations()
    }

    @Override
    void close() throws Exception {
        delegate.close()
    }
}
