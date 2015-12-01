#include <pebble.h>

#define KEY_BUTTON_UP   0
#define KEY_BUTTON_SELECT 1
#define KEY_BUTTON_DOWN 2
#define RESULT_KEY 0
#define START_BUTTON 0
#define VIBRATE_BUTTON 1
#define STOP_BUTTON 2

static Window *window;
static TextLayer *text_layer;
bool vibrateFlag;
AppTimer* vibe_timer = NULL;

//Helper Functions
static void vibeCallback(){
  
  if(vibrateFlag){
    text_layer_set_text(text_layer, "Vibrating");
    vibes_long_pulse();
    vibe_timer = app_timer_register(1000,vibeCallback,NULL);
  }
  else{
    text_layer_set_text(text_layer, "Vibrating stopped");
  }
}

//Communication Functions
static void outbox_sent_handler(DictionaryIterator *iter, void *context) {
  // Ready for next command
  //text_layer_set_text(text_layer, "Press up or down.");
}

static void outbox_failed_handler(DictionaryIterator *iter, AppMessageResult reason, void *context) {
  text_layer_set_text(text_layer, "Send failed!");
  APP_LOG(APP_LOG_LEVEL_ERROR, "Fail reason: %d", (int)reason);
}

static void inbox_received_handler(DictionaryIterator *iter, void *context) {
  // Ready for next command
  Tuple *messageReceived = dict_find(iter, RESULT_KEY);
  // text_layer_set_text(text_layer, "Received Data");
  switch(messageReceived->value->int32) {
    case START_BUTTON:
      text_layer_set_text(text_layer, "Start App");
      break;

    case VIBRATE_BUTTON:
      text_layer_set_text(text_layer, "Find Phone");
      vibrateFlag = true;
      vibe_timer = app_timer_register(1000,vibeCallback,NULL);
      break;

    case STOP_BUTTON:
      text_layer_set_text(text_layer, "Stop Vibrating");
      vibrateFlag = false;
      break;
  }

}

static void send(int key, int value) {
  DictionaryIterator *iter;
  app_message_outbox_begin(&iter);

  dict_write_int(iter, key, &value, sizeof(int), true);

  app_message_outbox_send();
}

//Button Functions

static void select_click_handler(ClickRecognizerRef recognizer, void *context) {
  text_layer_set_text(text_layer, "Select Stop Vibrating");
  send(KEY_BUTTON_SELECT,0);
}

static void up_click_handler(ClickRecognizerRef recognizer, void *context) {
  text_layer_set_text(text_layer, "Up Open app on Phone");
  send(KEY_BUTTON_UP,0);
}

static void down_click_handler(ClickRecognizerRef recognizer, void *context) {
  text_layer_set_text(text_layer, "Down Find Phone");
  send(KEY_BUTTON_DOWN,0);
}

static void click_config_provider(void *context) {
  window_single_click_subscribe(BUTTON_ID_SELECT, select_click_handler);
  window_single_click_subscribe(BUTTON_ID_UP, up_click_handler);
  window_single_click_subscribe(BUTTON_ID_DOWN, down_click_handler);
}

//Main Functions
static void window_load(Window *window) {
  Layer *window_layer = window_get_root_layer(window);
  GRect bounds = layer_get_bounds(window_layer);

  text_layer = text_layer_create((GRect) { .origin = { 0, 72 }, .size = { bounds.size.w, 20 } });
  text_layer_set_text(text_layer, "Press a button");
  text_layer_set_text_alignment(text_layer, GTextAlignmentCenter);
  layer_add_child(window_layer, text_layer_get_layer(text_layer));
}

static void window_unload(Window *window) {
  text_layer_destroy(text_layer);
}

static void init(void) {
  window = window_create();
  window_set_click_config_provider(window, click_config_provider);
  window_set_window_handlers(window, (WindowHandlers) {
    .load = window_load,
    .unload = window_unload,
  });
  const bool animated = true;
  window_stack_push(window, animated);

    // Open AppMessage
app_message_register_outbox_sent(outbox_sent_handler);
app_message_register_outbox_failed(outbox_failed_handler);
app_message_register_inbox_received(inbox_received_handler);
app_message_open(app_message_inbox_size_maximum(), app_message_outbox_size_maximum());
}

static void deinit(void) {
  window_destroy(window);
}

int main(void) {
  init();

  APP_LOG(APP_LOG_LEVEL_DEBUG, "Done initializing, pushed window: %p", window);

  app_event_loop();
  
  deinit();
}
