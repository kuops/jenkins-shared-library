def call(String lang) {
    switch (lang) {
        case 'java':
            JavaPipeline();
            break;
        default:
            error('not support lang')
    }
}
