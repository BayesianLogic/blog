
BLOG_HOME=../../..
time "$BLOG_HOME"/run.sh -e blog.engine.ParticleFilter \
  LocToInteger.blog \
  bird_flow_probs.blog \
  bird_features.blog \
  bird_model.blog \
  bird_obs.blog \
  bird_queries.blog \
  -n 1000 -q 1 -w out.json