package com.usterki.observer;

/**
 * Wzorzec projektowy: OBSERWATOR (Observer) — grupa czynnościowa.
 *
 * Problem: zmiana statusu zgłoszenia powinna wywoływać kilka niezależnych
 * skutków ubocznych — zapis do historii statusów, wpis do logów, a w przyszłości
 * powiadomienie e-mail. Wpisanie ich wszystkich wprost do serwisu zgłoszeń
 * silnie wiązało go z tymi mechanizmami i utrudniało dodawanie kolejnych.
 *
 * Rozwiązanie: obserwator subskrybuje zdarzenie zmiany statusu i reaguje na nie
 * we własnym zakresie. Podmiot ({@link PublikatorZmianyStatusu}) nie wie, ilu jest
 * obserwatorów ani co robią — można ich dodawać/usuwać bez modyfikacji serwisu.
 */
public interface ZmianaStatusuObserver {

    void onZmianaStatusu(ZdarzenieZmianyStatusu zdarzenie);
}
