package com.health.assessment.mapper;

import com.health.assessment.entity.HealthContent;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 健康内容库 Mapper
 */
@Mapper
public interface HealthContentMapper {

    @Select("SELECT * FROM t_health_content WHERE id = #{id}")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "content_title", property = "contentTitle"),
            @Result(column = "content_type", property = "contentType"),
            @Result(column = "content_body", property = "contentBody"),
            @Result(column = "related_diseases", property = "relatedDiseases"),
            @Result(column = "content_source", property = "contentSource"),
            @Result(column = "author", property = "author"),
            @Result(column = "is_published", property = "isPublished"),
            @Result(column = "published_at", property = "publishedAt"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt")
    })
    HealthContent selectById(Long id);

    @Select("SELECT * FROM t_health_content WHERE is_published = 1 ORDER BY published_at DESC")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "content_title", property = "contentTitle"),
            @Result(column = "content_type", property = "contentType"),
            @Result(column = "content_body", property = "contentBody"),
            @Result(column = "related_diseases", property = "relatedDiseases"),
            @Result(column = "content_source", property = "contentSource"),
            @Result(column = "author", property = "author"),
            @Result(column = "is_published", property = "isPublished"),
            @Result(column = "published_at", property = "publishedAt"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt")
    })
    List<HealthContent> selectAllPublished();

    @Select("SELECT * FROM t_health_content WHERE is_published = 1 AND content_type = #{contentType} " +
            "ORDER BY published_at DESC")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "content_title", property = "contentTitle"),
            @Result(column = "content_type", property = "contentType"),
            @Result(column = "content_body", property = "contentBody"),
            @Result(column = "related_diseases", property = "relatedDiseases"),
            @Result(column = "content_source", property = "contentSource"),
            @Result(column = "author", property = "author"),
            @Result(column = "is_published", property = "isPublished"),
            @Result(column = "published_at", property = "publishedAt"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt")
    })
    List<HealthContent> selectPublishedByType(String contentType);

    @Select("SELECT * FROM t_health_content WHERE is_published = 1 " +
            "AND MATCH(content_title, content_body) AGAINST(#{keyword} IN BOOLEAN MODE) " +
            "ORDER BY published_at DESC LIMIT 20")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "content_title", property = "contentTitle"),
            @Result(column = "content_type", property = "contentType"),
            @Result(column = "content_body", property = "contentBody"),
            @Result(column = "related_diseases", property = "relatedDiseases"),
            @Result(column = "content_source", property = "contentSource"),
            @Result(column = "author", property = "author"),
            @Result(column = "is_published", property = "isPublished"),
            @Result(column = "published_at", property = "publishedAt"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt")
    })
    List<HealthContent> searchByKeyword(String keyword);

    @Select("SELECT * FROM t_health_content WHERE is_published = 1 " +
            "AND (content_title LIKE CONCAT('%', #{keyword}, '%') OR content_body LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY published_at DESC LIMIT 20")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "content_title", property = "contentTitle"),
            @Result(column = "content_type", property = "contentType"),
            @Result(column = "content_body", property = "contentBody"),
            @Result(column = "related_diseases", property = "relatedDiseases"),
            @Result(column = "content_source", property = "contentSource"),
            @Result(column = "author", property = "author"),
            @Result(column = "is_published", property = "isPublished"),
            @Result(column = "published_at", property = "publishedAt"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt")
    })
    List<HealthContent> searchByKeywordLike(String keyword);

    @Select("SELECT * FROM t_health_content ORDER BY created_at DESC")
    @Results({
            @Result(column = "id", property = "id", id = true),
            @Result(column = "content_title", property = "contentTitle"),
            @Result(column = "content_type", property = "contentType"),
            @Result(column = "content_body", property = "contentBody"),
            @Result(column = "related_diseases", property = "relatedDiseases"),
            @Result(column = "content_source", property = "contentSource"),
            @Result(column = "author", property = "author"),
            @Result(column = "is_published", property = "isPublished"),
            @Result(column = "published_at", property = "publishedAt"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt")
    })
    List<HealthContent> selectAll();

    @Insert("INSERT INTO t_health_content(content_title, content_type, content_body, related_diseases, " +
            "content_source, author, is_published, published_at) " +
            "VALUES(#{contentTitle}, #{contentType}, #{contentBody}, #{relatedDiseases}, " +
            "#{contentSource}, #{author}, #{isPublished}, #{publishedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(HealthContent content);

    @Update("UPDATE t_health_content SET content_title=#{contentTitle}, content_type=#{contentType}, " +
            "content_body=#{contentBody}, related_diseases=#{relatedDiseases}, " +
            "content_source=#{contentSource}, author=#{author}, " +
            "is_published=#{isPublished}, published_at=#{publishedAt}, updated_at=NOW() " +
            "WHERE id=#{id}")
    int update(HealthContent content);

    @Delete("DELETE FROM t_health_content WHERE id = #{id}")
    int deleteById(Long id);
}

