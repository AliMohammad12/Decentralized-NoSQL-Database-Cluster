package atypon.cluster.client.annotation;

import atypon.cluster.client.service.ClusterCollectionService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class CollectionAnnotationScanner {
    private final ClusterCollectionService clusterCollectionService;
    public CollectionAnnotationScanner(ClusterCollectionService clusterCollectionService) {
        this.clusterCollectionService = clusterCollectionService;
    }
    @PostConstruct
    public void createCollectionsForAnnotatedClasses() throws IOException, ClassNotFoundException {
        List<Class<?>> annotatedClasses = scanForAnnotatedClasses();
        for (Class<?> annotatedClass : annotatedClasses) {
            clusterCollectionService.createCollection(annotatedClass);
        }
    }
    private List<Class<?>> scanForAnnotatedClasses() throws IOException, ClassNotFoundException {
        List<Class<?>> annotatedClasses = new ArrayList<>();
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(CreateCollection.class));
        SimpleMetadataReaderFactory metadataReaderFactory = new SimpleMetadataReaderFactory();
        for (BeanDefinition beanDefinition : scanner.findCandidateComponents("")) {
            MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(beanDefinition.getBeanClassName());
            Class<?> annotatedClass = Class.forName(metadataReader.getClassMetadata().getClassName());
            annotatedClasses.add(annotatedClass);
        }
        return annotatedClasses;
    }
}