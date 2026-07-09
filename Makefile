.PHONY: build test lint fix

build:
	./gradlew assembleDebug

test:
	./gradlew test

lint:
	./gradlew lint

fix:
	./gradlew assembleDebug --daemon
	@echo "Si falla, revisa los errores arriba."

hooks:
	git config core.hooksPath .githooks
	chmod +x .githooks/pre-push
	@echo "Hooks instalados."