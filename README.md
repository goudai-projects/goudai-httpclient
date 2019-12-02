# goudai-httpclient

编译期生成调用远端接口的本地实现.


## MAVEN

processor

```xml
        <dependency>
            <groupId>cloud.goudai.httpclient</groupId>
            <artifactId>httpclient-processor</artifactId>
            <version>1.0-SNAPSHOT</version>
            <optional>true</optional>
        </dependency>
```

## 使用

定义接口

```java
@GoudaiClient("good-service")
public interface GoodService {


    @GetMapping("{id}")
    ApiResult<GoodsVo> get(@PathVariable("id") String id);

    @GetMapping("list")
    ApiResult<List<GoodsVo>> list(GoodQueryModel query);

    @PostMapping
    ApiResult<GoodsVo> post(@RequestBody Goods goods);

    @PutMapping("{id}")
    ApiResult<GoodsVo> put(@PathVariable String id, @RequestBody Goods goods);

    @DeleteMapping("{id}")
    ApiResult<Void> delete(@PathVariable String id);

    @PatchMapping("{id}")
    ApiResult<GoodsVo> patch(@PathVariable String id, @RequestBody Goods goods);

}
```

自动生成实现类

```java

@CircuitBreaker(
    name = "good-service"
)
@Service("goodServiceConnector")
public class GoodServiceConnector implements GoodService {
    
  private RestTemplate restTemplate;

  @Value("${good-service.baseUrl:http://good-service}")
  private String baseUrl;

  @Autowired
  public GoodServiceConnector(RestTemplate restTemplate) {
    Assert.notNull(restTemplate, "restTemplate must not be null!");;
    this.restTemplate = restTemplate;
  }

  @Override
  public ApiResult<GoodsVo> get(String id) {
    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(this.baseUrl + "/{id}");
    Map<String, Object> uriVariables = new HashMap<>();
    uriVariables.put("id", id);
    HttpHeaders headers = new HttpHeaders();
    HttpEntity<Object> httpEntity = new HttpEntity<>(null, headers);
    URI uri = builder.uriVariables(uriVariables)
           .buildAndExpand()
           .toUri();
    return restTemplate.exchange(
                           uri,
                           HttpMethod.GET,
                           httpEntity,
                           new ParameterizedTypeReference<ApiResult<GoodsVo>>(){}
                         ).getBody();
  }

  @Override
  public ApiResult<List<GoodsVo>> list(GoodQueryModel query) {
    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(this.baseUrl + "/list");
    if(query.getId() != null) builder.queryParam("id",query.getId());
    if(query.getName() != null) builder.queryParam("name",query.getName());
    if(query.getPage() != null) builder.queryParam("page",query.getPage());
    HttpHeaders headers = new HttpHeaders();
    HttpEntity<Object> httpEntity = new HttpEntity<>(null, headers);
    URI uri = builder
           .buildAndExpand()
           .toUri();
    return restTemplate.exchange(
                           uri,
                           HttpMethod.GET,
                           httpEntity,
                           new ParameterizedTypeReference<ApiResult<List<GoodsVo>>>(){}
                         ).getBody();
  }

  @Override
  public ApiResult<GoodsVo> post(Goods goods) {
    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(this.baseUrl + "");
    HttpHeaders headers = new HttpHeaders();
    HttpEntity<Goods> httpEntity = new HttpEntity<>(goods, headers);
    URI uri = builder
           .buildAndExpand()
           .toUri();
    return restTemplate.exchange(
                           uri,
                           HttpMethod.POST,
                           httpEntity,
                           new ParameterizedTypeReference<ApiResult<GoodsVo>>(){}
                         ).getBody();
  }

  @Override
  public ApiResult<GoodsVo> put(String id, Goods goods) {
    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(this.baseUrl + "/{id}");
    List<Object> indexUriVariables = new LinkedList<>();
    indexUriVariables.add(0, id);
    HttpHeaders headers = new HttpHeaders();
    HttpEntity<Goods> httpEntity = new HttpEntity<>(goods, headers);
    URI uri = builder
           .buildAndExpand(indexUriVariables.toArray())
           .toUri();
    return restTemplate.exchange(
                           uri,
                           HttpMethod.PUT,
                           httpEntity,
                           new ParameterizedTypeReference<ApiResult<GoodsVo>>(){}
                         ).getBody();
  }

  @Override
  public ApiResult<Void> delete(String id) {
    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(this.baseUrl + "/{id}");
    List<Object> indexUriVariables = new LinkedList<>();
    indexUriVariables.add(0, id);
    HttpHeaders headers = new HttpHeaders();
    HttpEntity<Object> httpEntity = new HttpEntity<>(null, headers);
    URI uri = builder
           .buildAndExpand(indexUriVariables.toArray())
           .toUri();
    return restTemplate.exchange(
                           uri,
                           HttpMethod.DELETE,
                           httpEntity,
                           new ParameterizedTypeReference<ApiResult<Void>>(){}
                         ).getBody();
  }

  @Override
  public ApiResult<GoodsVo> patch(String id, Goods goods) {
    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(this.baseUrl + "/{id}");
    List<Object> indexUriVariables = new LinkedList<>();
    indexUriVariables.add(0, id);
    HttpHeaders headers = new HttpHeaders();
    HttpEntity<Goods> httpEntity = new HttpEntity<>(goods, headers);
    URI uri = builder
           .buildAndExpand(indexUriVariables.toArray())
           .toUri();
    return restTemplate.exchange(
                           uri,
                           HttpMethod.PATCH,
                           httpEntity,
                           new ParameterizedTypeReference<ApiResult<GoodsVo>>(){}
                         ).getBody();
  }
}
```
