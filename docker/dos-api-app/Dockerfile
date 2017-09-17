FROM vertx/vertx3

ENV VERTICLE_NAME uk.nhs.digital.eps.dos.service.DispenserInformationServiceVerticle
ENV VERTICLE_FILE dos-api.jar
ENV VERTICLE_CONFIG config.json

ENV VERTICLE_HOME /opt/verticles

EXPOSE 8086

COPY $VERTICLE_FILE $VERTICLE_HOME/
COPY $VERTICLE_CONFIG $VERTICLE_HOME/

WORKDIR $VERTICLE_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec vertx run $VERTICLE_NAME -cp $VERTICLE_HOME/* -conf $VERTICLE_HOME/config.json"]