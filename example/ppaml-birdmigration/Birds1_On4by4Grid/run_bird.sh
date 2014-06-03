
BLOG_HOME=../../..
time "$BLOG_HOME"/blog -r -e blog.engine.LiuWestFilter \
  LocToInteger.blog \
  bird_flow_probs.blog \
  bird_features.blog \
  bird_model.blog \
  bird_obs.blog \
  bird_queries.blog \
  -Prho=0.95 -n 100 -q 1 -w out.json