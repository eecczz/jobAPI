window.onload = () => {
    window.ui = SwaggerUIBundle({
        url: "/swagger.yaml", // OpenAPI 문서 경로
        dom_id: '#swagger-ui',
        presets: [
            SwaggerUIBundle.presets.apis,
            SwaggerUIStandalonePreset
        ],
        layout: "StandaloneLayout"
    });
};
