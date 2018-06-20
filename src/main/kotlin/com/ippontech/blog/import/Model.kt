package com.ippontech.blog.import

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL

//{
//    "posts": [
//        {
//            "id": "5b29bf2917b6ea00bf757993",
//            "title": "PLOP",
//            "slug": "plop",
//            "status": "draft",
//            "mobiledoc": "{\"version\":\"0.3.1\",\"markups\":[],\"atoms\":[],\"cards\":[[\"card-markdown\",{\"cardName\":\"card-markdown\",\"markdown\":\"plol\"}]],\"sections\":[[10,0]]}",
//            "created_at": "2018-06-20T02:42:49.000Z",
//            "updated_at": "2018-06-20T03:03:13.000Z",
//            "published_at": null,
//            "authors": [
//                {
//                    "id": "5a267e56dd54250018d6b54b"
//                }
//            ],
//            "feature_image": null,
//            "tags": []
//        }
//    ]
//}

data class GhostPosts(val posts: List<Post>)

@JsonInclude(NON_NULL)
data class Post(
        val id: String?,
        val title: String,
        val slug: String,
        val status: String,
        val mobiledoc: String,
        val created_at: String?,
        val updated_at: String?,
        val published_at: String?,
        val authors: List<Author>,
        val feature_image: String?,
        val tags: List<Tag>)

data class Tag(val id: String)

data class Author(val id: String)

data class MobileDoc(
        val version: String,
        val markups: List<String>,
        val atoms: List<String>,
        val cards: List<List<Any>>,
        val sections: List<List<Int>>
)

data class Card(
        val cardName: String,
        val markdown: String)
