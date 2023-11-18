
.PHONY

OTHER=other
LOCAL_REPO=localRepo

paparazzi-integration:
	rm -rf $(OTHER)
	git clone ssh://git@phabricator.tdrhq.com:2222/diffusion/23/paparazzi-example.git $(OTHER)

	cd $(OTHER)
