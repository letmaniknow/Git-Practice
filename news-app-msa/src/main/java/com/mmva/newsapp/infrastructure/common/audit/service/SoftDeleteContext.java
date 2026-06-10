package com.mmva.newsapp.infrastructure.common.audit.service;

import com.mmva.newsapp.infrastructure.common.audit.repository.SoftDeleteSpec;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Context-aware soft-delete specification provider.
 * 
 * <h3>Purpose:</h3>
 * <p>
 * Provides the appropriate soft-delete specification based on the current
 * request context (admindashboard vs public endpoint).
 * </p>
 * 
 * <h3>Design Philosophy:</h3>
 * <ul>
 * <li><b>Explicit over Implicit:</b> Code clearly shows what filtering is
 * applied</li>
 * <li><b>Testable:</b> Easy to mock for unit tests</li>
 * <li><b>No Magic:</b> No hidden filters or AOP interceptors</li>
 * <li><b>Flexible:</b> Services can override behavior when needed</li>
 * </ul>
 * 
 * <h3>Usage in Services:</h3>
 * 
 * <pre>{@code
 * @Service
 * public class NewsServiceImpl {
 *     private final SoftDeleteContext softDeleteContext;
 * 
 *     public Page<News> findAll(Pageable pageable) {
 *         // Automatically uses correct spec based on request context
 *         return newsRepository.findAll(
 *                 softDeleteContext.getSpec(),
 *                 pageable);
 *     }
 * 
 *     public List<News> findDeleted() {
 *         // Explicit override for admindashboard restore listing
 *         return newsRepository.findAll(SoftDeleteSpec.onlyDeleted());
 *     }
 * }
 * }</pre>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Slf4j
@Component
public class SoftDeleteContext {

    private static final String ADMIN_PATH_PREFIX = "/api/v1/admindashboard";
    private static final String INCLUDE_DELETED_PARAM = "includeDeleted";

    /**
     * Gets the appropriate specification based on current request context.
     * 
     * <ul>
     * <li>Admin endpoints: returns {@code includeDeleted()} by default</li>
     * <li>Admin with ?includeDeleted=false: returns {@code notDeleted()}</li>
     * <li>Public endpoints: returns {@code notDeleted()}</li>
     * <li>Non-web context: returns {@code notDeleted()} for safety</li>
     * </ul>
     * 
     * @param <T> the entity type
     * @return appropriate specification for current context
     */
    public <T> Specification<T> getSpec() {
        if (isAdminContext() && !isExplicitlyExcludingDeleted()) {
            log.debug("Admin context - including deleted records");
            return SoftDeleteSpec.includeDeleted();
        }
        log.debug("Public context - excluding deleted records");
        return SoftDeleteSpec.notDeleted();
    }

    /**
     * Gets specification for public-visible records (not deleted AND active).
     * Use this for public-facing APIs regardless of admindashboard context.
     * 
     * @param <T> the entity type
     * @return specification for publicly visible records
     */
    public <T> Specification<T> getPublicSpec() {
        return SoftDeleteSpec.publicVisible();
    }

    /**
     * Gets specification for only deleted records.
     * Use for admindashboard restore listing.
     * 
     * @param <T> the entity type
     * @return specification for deleted records only
     */
    public <T> Specification<T> getDeletedOnlySpec() {
        return SoftDeleteSpec.onlyDeleted();
    }

    /**
     * Checks if current request is an admindashboard endpoint.
     * 
     * @return true if admindashboard context, false otherwise
     */
    public boolean isAdminContext() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return false;
            }
            HttpServletRequest request = attrs.getRequest();
            return request.getRequestURI().startsWith(ADMIN_PATH_PREFIX);
        } catch (Exception e) {
            log.debug("Not in web context: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Checks if request explicitly excludes deleted records via query param.
     * 
     * @return true if ?includeDeleted=false is present
     */
    private boolean isExplicitlyExcludingDeleted() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return false;
            }
            HttpServletRequest request = attrs.getRequest();
            String param = request.getParameter(INCLUDE_DELETED_PARAM);
            return "false".equalsIgnoreCase(param);
        } catch (Exception e) {
            return false;
        }
    }
}
