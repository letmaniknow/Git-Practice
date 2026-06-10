package com.mmva.newsapp.infrastructure.requestanalytics.service;

import com.mmva.newsapp.infrastructure.requestanalytics.dto.RequestClientInfoDto;
import com.mmva.newsapp.infrastructure.requestanalytics.dto.RequestAnalyticsInfoDto;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

/**
 * Service interface for extracting information from HTTP requests.
 * 
 * <p>
 * Implementations provide methods to extract client IP, headers,
 * and other request metadata for analytics and audit purposes.
 * </p>
 * 
 * <h3>Naming Convention:</h3>
 * <p>
 * Per PROJECT_PRINCIPLES.md section 6.3, interfaces use the plain name
 * (e.g., {@code RequestInfoService}) and implementations use the Impl suffix
 * (e.g., {@code RequestInfoServiceImpl}). No "I" prefix is used per
 * Java/Spring conventions.
 * </p>
 * 
 * <h3>Usage:</h3>
 * 
 * <pre>{@code
 * @Autowired
 * private RequestInfoService requestInfoService;
 * 
 * // In a controller (with request)
 * ClientInfo info = requestInfoService.getClientInfo(request);
 * 
 * // In any service (without request)
 * ClientInfo info = requestInfoService.getCurrentClientInfo();
 * }</pre>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public interface RequestInfoService {

    /**
     * Extracts ALL possible information from the request for analytics.
     * 
     * @param request the HTTP servlet request
     * @return RequestAnalyticsInfo with all extracted information (96 fields)
     */
    RequestAnalyticsInfoDto getFullRequestInfo(HttpServletRequest request);

    /**
     * Extracts essential client information from the request.
     * Lightweight alternative to getFullRequestInfo.
     * 
     * @param request the HTTP servlet request
     * @return ClientInfo record containing essential information (9 fields)
     */
    RequestClientInfoDto getClientInfo(HttpServletRequest request);

    /**
     * Gets the current request's client info using RequestContextHolder.
     * Can be called from any service without passing the request.
     * 
     * @return ClientInfo for current request, or minimal info if not in request
     *         context
     */
    RequestClientInfoDto getCurrentClientInfo();

    /**
     * Gets the current request's full analytics info using RequestContextHolder.
     * 
     * @return RequestAnalyticsInfo for current request, or empty if not in request
     *         context
     */
    RequestAnalyticsInfoDto getCurrentFullRequestInfo();

    /**
     * Extracts the client IP address from the request.
     * Handles various proxy scenarios by checking multiple headers.
     * 
     * @param request the HTTP servlet request
     * @return the client IP address
     */
    String getClientIpAddress(HttpServletRequest request);

    /**
     * Gets the User-Agent header.
     * 
     * @param request the HTTP servlet request
     * @return the User-Agent string
     */
    String getUserAgent(HttpServletRequest request);

    /**
     * Gets all headers as a map. Header names are lowercased.
     * 
     * @param request the HTTP servlet request
     * @return map of header names to values
     */
    Map<String, String> getAllHeaders(HttpServletRequest request);

    /**
     * Gets security-relevant headers for audit logging.
     * 
     * @param request the HTTP servlet request
     * @return map of security header names to values
     */
    Map<String, String> getSecurityHeaders(HttpServletRequest request);

    /**
     * Checks if the request is secure (HTTPS).
     * 
     * @param request the HTTP servlet request
     * @return true if HTTPS
     */
    boolean isSecureRequest(HttpServletRequest request);

    /**
     * Gets the Accept-Language header.
     * 
     * @param request the HTTP servlet request
     * @return the Accept-Language string
     */
    String getAcceptLanguage(HttpServletRequest request);

    /**
     * Gets the Referer header.
     * 
     * @param request the HTTP servlet request
     * @return the Referer string
     */
    String getReferer(HttpServletRequest request);

    /**
     * Gets the Origin header.
     * 
     * @param request the HTTP servlet request
     * @return the Origin string
     */
    String getOrigin(HttpServletRequest request);

    /**
     * Gets the device type from client hints.
     * 
     * @param request the HTTP servlet request
     * @return the device type string
     */
    String getDeviceType(HttpServletRequest request);

    /**
     * Gets the infrastructure/OS from client hints.
     * 
     * @param request the HTTP servlet request
     * @return the infrastructure string
     */
    String getPlatform(HttpServletRequest request);
}
