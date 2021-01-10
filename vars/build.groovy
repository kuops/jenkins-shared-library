def call(String lang) {
    switch (lang) {
        case 'java':
            javaPipeline();
            break;
        case 'nodejs':
            nodejsPipeline();
            break;
        case 'docs':
            docsPipeline()
            break
        default:
            error('not support lang')
    }
}
